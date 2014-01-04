package com.norcode.bukkit.redischat;

import com.norcode.bukkit.redischat.chat.ClickAction;
import com.norcode.bukkit.redischat.chat.HoverAction;
import com.norcode.bukkit.redischat.chat.Text;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_7_R1.ChatComponentText;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTTagByte;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagShort;
import net.minecraft.server.v1_7_R1.NBTTagString;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatRenderer extends BukkitRunnable {

	private final Chat vaultChat;
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private Permission vaultPerms;
	private final NBTTagCompound baseTag;
	private RedisChat plugin;

	private final ConcurrentLinkedQueue<ChatMessage> messageQueue = new ConcurrentLinkedQueue<ChatMessage>();
	private final ConcurrentHashMap<String, Set<String>> channelMembers = new ConcurrentHashMap<String, Set<String>>();

	public Queue<ChatMessage> getMessageQueue() {
		return messageQueue;
	}

	public ChatRenderer(RedisChat plugin) {
		this.plugin = plugin;
		// setup vault perms for looking up players group. and vault chat for getting chat prefixes.
		RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
		vaultPerms = rsp.getProvider();
		if (vaultPerms == null) {
			throw new RuntimeException("Vault and a permissions plugin are required.");
		}

		RegisteredServiceProvider<Chat> rspc = plugin.getServer().getServicesManager().getRegistration(Chat.class);
		vaultChat = rspc.getProvider();
		if (vaultChat == null) {
			throw new RuntimeException("Vault and a permissions plugin are required.");
		}

		// Setup the "base" NBT Tag that we'll use to render sender's hover data
		this.baseTag = new NBTTagCompound();
		this.baseTag.set("id", new NBTTagShort((short) 1));
		this.baseTag.set("Damage", new NBTTagShort((short) 0));
		this.baseTag.set("Count", new NBTTagByte((byte) 1));
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound dispTag = new NBTTagCompound();
		tag.set("display", dispTag);
		this.baseTag.set("tag", tag);
	}

	public IChatBaseComponent formatSender(String senderName) {
		Player p = plugin.getServer().getPlayerExact(senderName);
		String rank = getPlayerRank(p);
		String rankColor = getGroupColor(rank);
		baseTag.getCompound("tag").getCompound("display").set("Name", new NBTTagString(rankColor + senderName + ChatColor.RESET));
		NBTTagList lore = new NBTTagList();
		lore.add(new NBTTagString(rankColor + rank));
		baseTag.getCompound("tag").getCompound("display").set("Lore", lore);
		return new Text(senderName)
				.setHover(HoverAction.SHOW_ITEM, new ChatComponentText(baseTag.toString()))
				.setClick(ClickAction.SUGGEST_COMMAND, "/msg " + senderName);
	}

	private String getGroupColor(String group) {
		if (group.equalsIgnoreCase("admin")) {
			return ChatColor.GOLD.toString();
		} else if (group.equalsIgnoreCase("mod")) {
			return ChatColor.LIGHT_PURPLE.toString();
		} else if (group.equalsIgnoreCase("vip")) {
			return ChatColor.BLUE.toString();
		} else if (group.equalsIgnoreCase("member")) {
			return ChatColor.GREEN.toString();
		}
		return ChatColor.WHITE.toString();
	}

	private String getPlayerRank(Player p) {
		String rank = StringUtils.capitalize(vaultPerms.getPrimaryGroup(p).toLowerCase());
		return rank;
	}

	public PacketPlayOutChat createChatPacket(ChatMessage msg) {
		Text prefix = new Text("");
		MessageType msgType = MessageType.fromPrefix(msg.getDestination().substring(0,1));
		switch (msgType) {
		case CHANNEL:
			String channel = msg.getDestination().substring(1);
			prefix.append(ChatColor.GRAY + "[" + ChatColor.YELLOW + channel + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		case LOCAL:
			prefix.append(ChatColor.GRAY + "[" + ChatColor.YELLOW + "L" + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		case BROADCAST:
			prefix.append(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		case PRIVATE:
			prefix.append(ChatColor.GRAY + "[" + ChatColor.AQUA + "@" + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		}
		prefix.append(ChatColor.DARK_GRAY + "<").append(formatSender(msg.getSender())).append(ChatColor.DARK_GRAY + "> ").append(new Text(msg.getMessage()));
		PacketPlayOutChat packet = new PacketPlayOutChat(prefix, true);
		return packet;
	}

	public void send(Player player, PacketPlayOutChat packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public void run() {
		while (!messageQueue.isEmpty()) {
			ChatMessage msg = messageQueue.poll();
			// check out msg.getDestination and decide who gets this, for now, everyone does.
			PacketPlayOutChat packet = createChatPacket(msg);
			MessageType msgType = MessageType.fromPrefix(msg.getDestination().substring(0,1));
			switch (msgType) {
			case BROADCAST:
				for (Player p: plugin.getServer().getOnlinePlayers()) {
					send(p, packet);
				}
				break;
			case CHANNEL:
				Channel chan = plugin.getChannelManager().getChannel(msg.getDestination().substring(1));
				if (chan != null && chan.isMember(msg.getSender())) {
					for (String name: chan.getMembers()) {
						send(Bukkit.getPlayerExact(name), packet);
					}
				}
				break;
			case LOCAL:
				String[] parts = msg.getDestination().substring(1).split(";");
				World w = plugin.getServer().getWorld(parts[0]);
				int x = Integer.parseInt(parts[1]);
				int y = Integer.parseInt(parts[2]);
				int z = Integer.parseInt(parts[3]);
				int radius = Integer.parseInt(parts[4]);
				Player sender = plugin.getServer().getPlayerExact(msg.getSender());
				for (Player p: sender.getWorld().getPlayers()) {
					if (p.getLocation().distance(sender.getLocation()) < radius) {
						send(p, packet);
					}
				}
				break;
			case PRIVATE:
				String recip = msg.getDestination().substring(1);
				Player p = plugin.getServer().getPlayerExact(recip);
				if (p != null) {
					send(p, packet);
				}
				break;
			}
		}
	}
}
