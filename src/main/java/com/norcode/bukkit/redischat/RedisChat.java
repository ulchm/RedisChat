package com.norcode.bukkit.redischat;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.command.ChannelCommand;
import com.norcode.bukkit.redischat.command.PrivateMessageCommand;
import com.norcode.bukkit.redischat.command.ReplyCommand;
import com.norcode.bukkit.redischat.listeners.PubSubListener;
import net.minecraft.server.v1_7_R1.ChatBaseComponent;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RedisChat extends JavaPlugin implements Listener {

	int LOCAL_RADIUS = 64;
	private JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379, 0);
	private PubSubListener pubSubListener = new PubSubListener(this);
	private Gson gson = new Gson();
	private ChatRenderer chatRenderer;
	private ChannelManager channelManager;
	private ChannelCommand channelCommand;
	private PrivateMessageCommand pmCommand;
	private ReplyCommand replyCommand;

	private static boolean debugMode = true;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		chatRenderer = new ChatRenderer(this);
		chatRenderer.runTaskTimer(this, 1, 1);
		channelManager = new ChannelManager(this);
		channelCommand = new ChannelCommand(this);
		replyCommand = new ReplyCommand(this);
		pmCommand = new PrivateMessageCommand(this);
		Thread listenerThread = new Thread(pubSubListener);
		listenerThread.start();
	}

	public static void debug(Object o) {
		if (debugMode) {
			Bukkit.getLogger().info(o.toString());
		}
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
				// First check for 'channel switches' which aren't real messages.
				if (event.getMessage().startsWith("#") && !event.getMessage().contains(" ")) {
					String channel = event.getMessage().substring(1).toUpperCase();
					Channel c = channelManager.getChannel(channel);
					if (c == null || !channelManager.setFocusedChannel(event.getPlayer(), channel)) {
						event.getPlayer().sendMessage(ChatColor.RED + "Unknown Channel: " + channel);
						return;
					}
					event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "You are now chatting in #" + channel);
					event.setCancelled(true);
					return;
				}
				// Now check for a message prefix and adjust the 'channel' were sending to
				// we use 'channel' loosely here, meaning the redis channel name,
				// it could actually be a local message or PM.
				String channel = null;
				if (event.getMessage().startsWith("@")) {
					String[] parts = event.getMessage().substring(1).split(" ", 2);
					Player target = null;
					if (parts[0].trim().equals("")) {
						if (!event.getPlayer().hasMetadata(MetaKeys.PM_REPLY_TO)) {
							send(event.getPlayer(), ChatColor.RED + "You do not have an ongoing conversation.");
							return;
						}
						String targetName = event.getPlayer().getMetadata(MetaKeys.PM_REPLY_TO).get(0).asString();
						target = Bukkit.getPlayerExact(targetName);
						if (target == null) {
							send(event.getPlayer(), ChatColor.RED + targetName + " is no longer online.");
							return;
						}
					} else {
						List<Player> matches = new ArrayList<Player>();
						for (Player p: getServer().getOnlinePlayers()) {
							if (event.getPlayer().canSee(p) && p.getName().startsWith(parts[0])) {
								matches.add(p);
							}
						}
						if (matches.size() == 0) {
							send(event.getPlayer(), ChatColor.RED + "Unknown Player: " + parts[0]);
							return;
						} else if (matches.size() > 1) {
							send(event.getPlayer(), ChatColor.RED + "'"+ parts[0] + "' matches more than 1 player!");
							return;
						}
						target = matches.get(0);
						if (target.getName().equals(event.getPlayer().getName())) {
							send(event.getPlayer(), ChatColor.BOLD + " * " + ChatColor.RESET + "" +
									ChatColor.GOLD + "" + ChatColor.ITALIC + "You mumble to yourself, but you can't quite understand what you're trying to say.");
							return;
						}

					}
					event.getPlayer().setMetadata(MetaKeys.PM_REPLY_TO, new FixedMetadataValue(RedisChat.this, target.getName()));
					channel = "@" + target.getName();
					event.setMessage(parts[1]);
				} else if (event.getMessage().startsWith("#")) {
					String[] parts = event.getMessage().substring(1).split(" ", 2);
					String chanName = parts[0];
					Channel chan = null;
					for (Channel c: channelManager.getPlayerChannels(event.getPlayer())) {
						if (c.getName().toLowerCase().equalsIgnoreCase(chanName)) {
							chan = c;
							break;
						}
					}
					if (chan == null) {
						send(event.getPlayer(), ChatColor.RED + "Unknown Channel: " + chanName);
						return;
					}
					event.setMessage(parts[1]);
					channel = "#" + chan.getName();
				} else {
					Channel c = channelManager.getFocusedChannel(event.getPlayer());
					if (c == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "You are not in any channels!");
						return;
					}
					channel = "#" + c.getName();
				}
				ChatMessage chatMessage = new ChatMessage(event.getPlayer().getName(), channel, event.getMessage(), System.currentTimeMillis());
				Jedis jedis = jedisPool.getResource();
				jedis.publish("chat:" + channel, gson.toJson(chatMessage));
				jedisPool.returnResource(jedis);
			}
		});
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		List<String> channelList;
		if (!event.getPlayer().hasMetadata(MetaKeys.CHANNEL_LIST)) {
			event.getPlayer().setMetadata(MetaKeys.CHANNEL_LIST, new FixedMetadataValue(this, new LinkedList<String>()));
			ConfigurationSection cfg = PlayerID.getPlayerData(getName(), event.getPlayer());
			List<String> channels;
			if (!cfg.contains(MetaKeys.CHANNEL_LIST)) {
				channels = new ArrayList<String>();
				channels.add("G");
				cfg.set(MetaKeys.CHANNEL_LIST, channels);
				PlayerID.savePlayerData(getName(), event.getPlayer(), cfg);
			}
			channels = cfg.getStringList(MetaKeys.CHANNEL_LIST);
			channelList = new ArrayList<String>(channels);
		} else {
			channelList = new ArrayList<String>((Collection<? extends String>) event.getPlayer().getMetadata(MetaKeys.CHANNEL_LIST).get(0).value());
		}
		Collections.reverse(channelList);
		for (String c: channelList) {
			if (channelManager.channelExists(c)) {
				channelManager.joinChannel(event.getPlayer(), channelManager.getChannel(c), null);
			}
		}

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		for (Channel c: channelManager.getPlayerChannels(event.getPlayer())) {
			c.getMembers().remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerTabComplete(PlayerChatTabCompleteEvent event) {
		List<String> results = new ArrayList<String>();
		if (event.getLastToken().startsWith("@")) {
			for (Player p: getServer().getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(event.getLastToken().substring(1).toLowerCase())
						&& !event.getPlayer().getName().equals(p.getName()) && event.getPlayer().canSee(p)) {
					results.add("@" + p.getName());
				}
			}
		} else if (event.getLastToken().startsWith("#") && event.getChatMessage().equals(event.getLastToken())) {
			for (Channel c: channelManager.getPlayerChannels(event.getPlayer())) {
				if (c.getName().toLowerCase().startsWith(event.getLastToken().substring(1).toLowerCase())) {
					results.add("#" + c.getName());
				}
			}
		}
		event.getTabCompletions().clear();
		event.getTabCompletions().addAll(results);
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