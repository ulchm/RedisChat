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
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern colorPattern = Pattern.compile("(\u00A7[0-9a-fklmnor])");
	private static final Pattern linkPattern = Pattern.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))");

	public Text formatChatMessage(String message, Text baseFormat, Player sender) {
		boolean canColor = sender.hasPermission("redischat.color-codes");
		boolean canLink = sender.hasPermission("redischat.link-urls");
		Matcher m = colorPattern.matcher(ChatColor.translateAlternateColorCodes('&', message));
		StringBuffer buf;
		Text root = baseFormat;
		Text working = root;
		Text tmp;
		String tail;
		while (m.find()) {
			buf = new StringBuffer();
			char c = m.group().charAt(1);
			switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					if (canColor) {
						tmp = new Text("").setColor(ChatColor.getByChar(c));
						working.append(tmp);
						working = tmp;
					}
					break;
				case 'k':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					if (canColor) {
						tmp = new Text("").setRandom(true);
						working.append(tmp);
						working = tmp;
					}
					break;
				case 'l':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					if (canColor) {
						tmp = new Text("").setBold(true);
						working.append(tmp);
						working = tmp;
					}
					break;
				case 'm':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					if (canColor) {
						tmp = new Text("").setStrikethrough(true);
						working.append(tmp);
						working = tmp;
					}
					break;
				case 'n':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					if (canColor) {
						tmp = new Text("").setUnderline(true);
						working.append(tmp);
						working = tmp;
					}
					break;
				case 'r':
					m.appendReplacement(buf, "");
					working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
					working = root;
					break;
			}
		}
		buf = new StringBuffer();
		m.appendTail(buf);
		if (buf.toString().length() > 0) {
			working.append(canLink ? linkifyMessage(buf.toString()) : new Text(buf.toString()));
		}
		return root;
	}

	public static Text linkifyMessage(String msg) {
		Text text = new Text("");
		Matcher m = linkPattern.matcher(msg);
		while(m.find()) {
			String url = m.group();
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}
			StringBuffer buf = new StringBuffer();
			m.appendReplacement(buf, "");
			text.append(new Text(buf.toString()));
			text.append(m.group())
					.setClick(ClickAction.OPEN_URL, url)
					.setHover(HoverAction.SHOW_TEXT, new Text("Click to open this url in your web browser."));
		}
		StringBuffer buf = new StringBuffer();
		m.appendTail(buf);
		text.append(buf.toString());
		return text;
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

	public IChatBaseComponent formatPlayerName(String playerName) {
		Player p = plugin.getServer().getPlayerExact(playerName);
		String rank = getPlayerRank(p);
		String rankColor = getGroupColor(rank);
		baseTag.getCompound("tag").getCompound("display").set("Name", new NBTTagString(rankColor + playerName + ChatColor.RESET));
		NBTTagList lore = new NBTTagList();
		lore.add(new NBTTagString(rankColor + rank));
		baseTag.getCompound("tag").getCompound("display").set("Lore", lore);
		return new Text(rankColor + playerName)
				.setHover(HoverAction.SHOW_ITEM, new ChatComponentText(baseTag.toString()))
				.setClick(ClickAction.SUGGEST_COMMAND, "/msg " + playerName);
	}

	private String getGroupColor(String group) {
		String s = ChatColor.translateAlternateColorCodes('&', vaultChat.getGroupPrefix((String) null, group));
		if (s == null) {
			s = "";
		}
		return s;
	}

	private String getPlayerRank(Player p) {
		String rank = StringUtils.capitalize(vaultPerms.getPrimaryGroup(p).toLowerCase());
		return rank;
	}

	public PacketPlayOutChat createChatPacket(ChatMessage msg) {
		Text prefix = new Text("");
		MessageType msgType = MessageType.fromPrefix(msg.getDestination().substring(0,1));
		String textColor = "";
		switch (msgType) {
		case CHANNEL:
			boolean isYelling = false;
			String chanName = msg.getDestination().substring(1);

			if (chanName.endsWith("!")) {
				isYelling = true;
				chanName = chanName.substring(0, chanName.length() - 1);
			}
			Channel c = plugin.getChannelManager().getChannel(chanName);
			String nameColor = c.getNameColor();
			if (isYelling) nameColor += ChatColor.BOLD.toString();
			textColor = c.getTextColor();
			prefix.append(ChatColor.GRAY + "[" + nameColor + c.getName() + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		case BROADCAST:
			prefix.append(ChatColor.GRAY + "[" + ChatColor.RED + "!" + ChatColor.GRAY + "] " + ChatColor.RESET);
			break;
		}
		Text text = new Text("");
		Player sender = plugin.getServer().getPlayerExact(msg.getSender());
		if (textColor.length() == 2) {
			text.setColor(ChatColor.getByChar(textColor.charAt(1)));
		}
		Text formatted = formatChatMessage(msg.getMessage(), text, sender);
		prefix.append(ChatColor.DARK_GRAY + "<").append(formatPlayerName(msg.getSender())).append(ChatColor.DARK_GRAY + "> ").append(formatted);
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
			plugin.debug(msg);
			// check out msg.getDestination and decide who gets this, for now, everyone does.
			MessageType msgType = MessageType.fromPrefix(msg.getDestination().substring(0,1));
			PacketPlayOutChat packet;
			switch (msgType) {
			case BROADCAST:
				packet = createChatPacket(msg);
				for (Player p: plugin.getServer().getOnlinePlayers()) {
					send(p, packet);
				}
				break;
			case CHANNEL:
				packet = createChatPacket(msg);
				boolean isYelling = false;
				String chanName = msg.getDestination().substring(1);
				if (chanName.endsWith("!")) {
					isYelling = true;
					chanName = chanName.substring(0, chanName.length() - 1);
				}
				Channel chan = plugin.getChannelManager().getChannel(chanName);
				if (chan != null && chan.isMember(msg.getSender())) {
					Player sender = Bukkit.getPlayerExact(msg.getSender());
					for (String memberName: chan.getMembers()) {
						if (chan.getRadius() == -1 || isYelling) {
							send(Bukkit.getPlayerExact(memberName), packet);
						} else {
							Player target = Bukkit.getPlayerExact(memberName);
							if (target.getWorld().equals(sender.getWorld())) {
								if (target.getLocation().distance(sender.getLocation()) < chan.getRadius()) {
									send(target, packet);
								}
							}
						}
					}
				}
				break;
			case PRIVATE:
				Player recip = plugin.getServer().getPlayerExact(msg.getDestination().substring(1));
				plugin.notifyPrivateMessage(recip);
				Player sender = plugin.getServer().getPlayerExact(msg.getSender());
				recip.setMetadata(MetaKeys.PM_REPLY_TO, new FixedMetadataValue(plugin, sender.getName()));
				sender.setMetadata(MetaKeys.PM_REPLY_TO, new FixedMetadataValue(plugin, recip.getName()));
				if (recip != null) {
					packet = new PacketPlayOutChat(formatIncomingPM(msg), true);
					send(recip, packet);
					PacketPlayOutChat packet2 = new PacketPlayOutChat(formatOutgoingPM(msg), true);
					send(sender, packet2);
				}
				break;
			}
		}
	}

	private IChatBaseComponent formatIncomingPM(ChatMessage msg) {
		return new Text("")
				.append(ChatColor.GRAY + "[")
				.append(formatPlayerName(msg.getSender()))
				.append(ChatColor.DARK_AQUA + " ▶ " + ChatColor.GRAY + "You] ")
				.append(msg.getMessage());
	}

	private IChatBaseComponent formatOutgoingPM(ChatMessage msg) {
		return new Text("")
				.append(ChatColor.GRAY + "[You" + ChatColor.DARK_AQUA + " ▶ ")
				.append(formatPlayerName(msg.getDestination().substring(1)))
				.append(ChatColor.GRAY + "] ")
				.append(msg.getMessage());

	}
}
