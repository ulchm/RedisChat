package com.norcode.bukkit.redischat;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.command.ChannelCommand;
import com.norcode.bukkit.redischat.listeners.PubSubListener;
import net.minecraft.server.v1_7_R1.ChatBaseComponent;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedisChat extends JavaPlugin implements Listener {

	int LOCAL_RADIUS = 64;
	private JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379, 0);
	private PubSubListener pubSubListener = new PubSubListener(this);
	private Gson gson = new Gson();
	private ChatRenderer chatRenderer;
	private ChannelManager channelManager;
	private ChannelCommand channelCommand;
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		chatRenderer = new ChatRenderer(this);
		chatRenderer.runTaskTimer(this, 1, 1);
		channelManager = new ChannelManager(this);
		channelCommand = new ChannelCommand(this);
		Thread listenerThread = new Thread(pubSubListener);
		listenerThread.start();
	}


	@Override
	public void onDisable() {
		pubSubListener.stopRunning();
		chatRenderer.cancel();
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	@EventHandler(ignoreCancelled = true, priority= EventPriority.MONITOR)
	public void asyncPlayerChatEvent(final AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		getServer().getScheduler().runTask(this, new Runnable() {

			@Override
			public void run() {
				Jedis jedis = jedisPool.getResource();
				String channel = "G";
				// First check for 'channel switches' which aren't real messages.
				if (event.getMessage().startsWith("#") && !event.getMessage().contains(" ")) {
					channel = event.getMessage().substring(1).toUpperCase();
					channelManager.setFocusedChannel(event.getPlayer(), channel);
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
					channel = "#" + channelManager.getFocusedChannel(event.getPlayer()).getName().toLowerCase();
					if (channel.equals("#G")) {
						Location l = event.getPlayer().getLocation();
						channel = "@" + l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ() + ";" + LOCAL_RADIUS;
					}
				}
				ChatMessage chatMessage = new ChatMessage(event.getPlayer().getName(), channel, event.getMessage(), System.currentTimeMillis());
				jedis.publish("chat:" + channel, gson.toJson(chatMessage));
				jedisPool.returnResource(jedis);
			}
		});
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
			List<String> channelList = new ArrayList<String>(channels);
			Collections.reverse(channelList);
			for (String c: channelList) {
				if (channelManager.channelExists(c)) {
					channelManager.joinChannel(event.getPlayer(), channelManager.getChannel(c), null);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		for (Channel c: channelManager.getPlayerChannels(event.getPlayer())) {
			c.getMembers().remove(event.getPlayer().getName());
		}
	}

	public ChatRenderer getChatRenderer() {
		return chatRenderer;
	}

	public static void send(CommandSender player, Object... lines) {
		for (Object line : lines) {
			if (line instanceof String) {
				player.sendMessage((String) line);
			} else if (line instanceof IChatBaseComponent) {
				send(player, (IChatBaseComponent) line);
			} else {
				Bukkit.getLogger().info("Cannot send unknown type: " + line);
			}
		}
	}

	public static void send(CommandSender player, ChatBaseComponent chat) {
		PacketPlayOutChat packet = new PacketPlayOutChat(chat, true);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public Gson getGson() {
		return gson;
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}
}