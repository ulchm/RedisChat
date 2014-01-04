package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChannelInfoCommand extends BaseCommand {
	public ChannelInfoCommand(RedisChat plugin) {
		super(plugin, "info", new String[] {}, "redischat.command.channel.info", new String[]{});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		String name = args.peek();
		Channel channel;
		if (name == null) {
			channel = plugin.getChannelManager().getFocusedChannel((Player) commandSender);
		} else {
			channel = plugin.getChannelManager().getChannel(name);
			if (channel == null) {
				throw new CommandError("Unknown Channel '" + name + "'");
			}
			if (!(channel.isListed() || channel.getMembers().contains(commandSender.getName()))) {
				throw new CommandError("You do not have permission to view that channel.");
			}
		}
		commandSender.sendMessage("Channel Name: " + ChatColor.GOLD + channel.getName());
		commandSender.sendMessage("Password: " + (channel.getPassword() == null ? "none" : channel.getPassword()));
		commandSender.sendMessage("Owner: " + (channel.getOwnerId() == null ? "Nobody" : PlayerID.getOfflinePlayer(channel.getOwnerId()).getName()));
		commandSender.sendMessage("Listed?: " + channel.isListed());
		commandSender.sendMessage("Join Permission: " + (channel.getJoinPermission() == null ? "none" : channel.getJoinPermission()));
		commandSender.sendMessage("Chat Permission: " + (channel.getChatPermission() == null ? "none" : channel.getChatPermission()));
		String color = channel.getNameColor();
		ChatColor clr = ChatColor.getByChar(color.charAt(1));
		commandSender.sendMessage("Channel Name Color: " + clr + clr.name());
		color = channel.getTextColor();
		clr = ChatColor.getByChar(color.charAt(1));
		commandSender.sendMessage("Text Color: " + clr + clr.name());
		String members = "";
		for (String s: channel.getMembers()) {
			members += Bukkit.getPlayerExact(s).getDisplayName() + ", ";
		}
		if (members.endsWith(", ")) {
			members = members.substring(0, members.length()-2);
		}
		commandSender.sendMessage("Members: " + members);
	}

	@Override
	protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
		List<String> results = new ArrayList<String>();
		for (Channel c: plugin.getChannelManager().getAllChannels()) {
			if (c.isListed() || c.isMember(sender.getName()) || sender.hasPermission(c.getJoinPermission())) {
				results.add(c.getName());
			}
		}
		return results;
	}
}
