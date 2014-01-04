package com.norcode.bukkit.redischat;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.listeners.PubSubListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RedisChat extends JavaPlugin implements Listener {

	int LOCAL_RADIUS = 64;
    private JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379, 0);
    private PubSubListener pubSubListener = new PubSubListener(this);
    private Gson gson = new Gson();
	private ChatManager chatManager;

	@Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
		chatManager = new ChatManager(this);
		chatManager.runTaskTimer(this, 1, 1);
        Thread listenerThread = new Thread(pubSubListener);
        listenerThread.start();
    }

    @Override
    public void onDisable() {
        pubSubListener.stopRunning();
		chatManager.cancel();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @EventHandler(ignoreCancelled = true)
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {

        Jedis jedis = jedisPool.getResource();
		String channel = "G";
		// First check for 'channel switches' which aren't real messages.
		if (event.getMessage().length() == 2 && event.getMessage().startsWith("#")) {
			channel = event.getMessage().substring(1).toUpperCase();
			chatManager.setFocusedChannel(event.getPlayer(), channel);
			event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "You are now chatting in #" + channel);
			event.setCancelled(true);
			return;
		}
		// Now check for a message prefix and adjust the 'channel' were sending to
		// we use 'channel' loosely here, meaning the redis channel name,
		// it could actually be a local message or PM.
		if (event.getMessage().startsWith("#")) {
			String[] parts = event.getMessage().split(" ", 2);
			channel = "#" + parts[0].substring(1).toUpperCase();
			event.setMessage(parts[1]);
		} else if (event.getMessage().startsWith("@")) {
			String[] parts = event.getMessage().split(" ", 2);
			channel = "%" + parts[0].substring(1);
			event.setMessage(parts[1]);
		} else {
			channel = "#" + chatManager.getFocusedChannel(event.getPlayer());
			if (channel.equals("#G")) {
				Location l = event.getPlayer().getLocation();
				channel = "@" + l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ() + ";" + LOCAL_RADIUS;
			}

		}
		ChatMessage chatMessage = new ChatMessage(event.getPlayer().getName(), channel, event.getMessage(), System.currentTimeMillis());
        jedis.publish("chat:" + channel, gson.toJson(chatMessage));
        jedisPool.returnResource(jedis);
        event.setCancelled(true);
    }



	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!event.getPlayer().hasMetadata("channel-list")) {
			ConfigurationSection cfg = PlayerID.getPlayerData(getName(), event.getPlayer());
			List<String> channels;
			if (!cfg.contains("channel-list")) {
				channels = new ArrayList<String>();
				channels.add("G");
				cfg.set("channel-list", channels);
				PlayerID.savePlayerData(getName(), event.getPlayer(), cfg);
			}
			channels = cfg.getStringList("channel-list");
			LinkedList<String> channelList = new LinkedList<String>();
			for (String c: channels) {
				channelList.add(c);
				chatManager.getChannelPlayers(c).add(event.getPlayer().getName());
			}
			event.getPlayer().setMetadata("channel-list", new FixedMetadataValue(this, channelList));

		}
	}

	public ChatManager getChatManager() {
		return chatManager;
	}
}