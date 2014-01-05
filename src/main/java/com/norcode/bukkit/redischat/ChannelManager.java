package com.norcode.bukkit.redischat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChannelManager {

	private RedisChat plugin;

	private HashMap<String, Channel> channels = new HashMap<String, Channel>();

	public ChannelManager(RedisChat plugin) {
		this.plugin = plugin;
		this.initializeChannels();
	}

	private void initializeChannels() {
		Jedis j = plugin.getJedisPool().getResource();
		Map<String, String> channelData = j.hgetAll("channels");
		long now = System.currentTimeMillis();
		long expireAfter = plugin.getConfig().getLong("channel-expiry", TimeUnit.DAYS.toMillis(90));
		for (Map.Entry<String, String> entry: channelData.entrySet()) {
			final Channel c = plugin.getGson().fromJson(entry.getValue(), Channel.class);
			if (!c.isNoDelete() && (now - c.getLastJoinedAt() > expireAfter)) {
				new BukkitRunnable() {
					@Override
					public void run() {
						Jedis j = plugin.getJedisPool().getResource();
						j.hdel("channels", c.getName());
						plugin.getJedisPool().returnResource(j);
					}
				}.runTaskAsynchronously(plugin);
				continue;
			}
			channels.put(entry.getKey().toLowerCase(), c);
		}
		ConfigurationSection defChans = plugin.getConfig().getConfigurationSection("default-channels");
		for (String name: defChans.getKeys(false)) {
			if (!channels.containsKey(name.toLowerCase())) {
				Channel c =  new Channel();
				c.setName(name);
				c.setAutoJoin(defChans.getBoolean(name + ".autojoin", false));
				c.setNoDelete(defChans.getBoolean(name+".nodelete", false));
				c.setNameColor(defChans.getString(name+".namecolor", ""));
				c.setTextColor(defChans.getString(name + ".textcolor", ""));
				c.setListed(defChans.getBoolean(name+".listed", true));
				c.setChatPermission(defChans.getString(name+".chatpermission"));
				c.setJoinPermission(defChans.getString(name + ".joinpermission"));
				c.setDescription(defChans.getString(name+".description"));
				String uuids = defChans.getString(name+".ownerid");
				if (uuids != null) {
					c.setOwnerId(UUID.fromString(uuids));
				}
				for (String ids: defChans.getStringList(name+".operators")) {
					c.getOpIdSet().add(UUID.fromString(ids));
				}
				c.setPassword(defChans.getString(name+".password"));
				c.setRadius(defChans.getInt("radius", -1));
				saveChannel(c);
			}
		}
	}

	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}

	public boolean channelExists(String name) {
		return channels.containsKey(name.toLowerCase());
	}

	public List<Channel> getPlayerChannels(Player player) {
		LinkedList<String> pc = (LinkedList<String>) player.getMetadata(MetaKeys.CHANNEL_LIST).get(0).value();
		List<Channel> results = new ArrayList<Channel>(pc.size());
		for (String cn: pc) {
			Channel c = channels.get(cn);
			if (c != null) {
				results.add(c);
			}
		}
		return results;
	}

	public void saveChannel(Channel c) {
		if (!channels.containsKey(c.getName().toLowerCase())) {
			// new channel. set creation timestamp
			c.setCreatedAt(System.currentTimeMillis());
			c.setLastJoinedAt(c.getCreatedAt());
		}
		channels.put(c.getName().toLowerCase(), c);
		final String name = c.getName();
		final String encoded = plugin.getGson().toJson(c);
		// Save it asynchronously.
		new BukkitRunnable() {
			@Override
			public void run() {
				Jedis j = plugin.getJedisPool().getResource();
				j.hset("channels", name, encoded);
				plugin.getJedisPool().returnResource(j);
			}
		}.runTaskAsynchronously(plugin);
	}

	public Collection<Channel> getAllChannels() {
		return channels.values();
	}

	public boolean setFocusedChannel(Player player, String channel) {
		LinkedList<String> pc = (LinkedList<String>) player.getMetadata(MetaKeys.CHANNEL_LIST).get(0).value();
		if (!pc.contains(channel.toLowerCase())) {
			Channel c = getChannel(channel);
			return joinChannel(player, c, null);
		} else {
			pc.remove(channel.toLowerCase());
			pc.add(0, channel.toLowerCase());
			return true;
		}
	}

	public Channel getFocusedChannel(Player player) {
		LinkedList<String> pc = (LinkedList<String>) player.getMetadata(MetaKeys.CHANNEL_LIST).get(0).value();
		return channels.get(pc.peek().toLowerCase());
	}

	public boolean joinChannel(Player player, Channel channel, String password) {
		if (channel.getPassword() != null && !channel.getPassword().equals(password)) {
			return false;
		}

		if (channel.getJoinPermission() != null && !player.hasPermission(channel.getJoinPermission())) {
			return false;
		}

		LinkedList<String> pc = (LinkedList<String>) player.getMetadata(MetaKeys.CHANNEL_LIST).get(0).value();
		pc.add(0, channel.getName().toLowerCase());
		channel.getMembers().add(player.getName());
		channel.setLastJoinedAt(System.currentTimeMillis());
		plugin.getChannelManager().saveChannel(channel);
		return true;
	}

	public void leaveChannel(Player player, Channel channel) {
		channel.getMembers().remove(player.getName());
		LinkedList<String> pc = (LinkedList<String>) player.getMetadata(MetaKeys.CHANNEL_LIST).get(0).value();
		pc.remove(channel.getName().toLowerCase());
	}

	public Set<String> getChannelMembers(String channelName) {
		Channel c = channels.get(channelName.toLowerCase());
		return c.getMembers();
	}
}
