package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChannelLeaveCommand extends BaseCommand {
	public ChannelLeaveCommand(RedisChat plugin) {
		super(plugin, "leave", new String[] {"part", "p", "l"}, "redischat.command.channel.leave", new String[]{});
	}

	@Override
	protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
		List<String> results = new ArrayList<String>();
		String p = args.peek().toLowerCase();
		if (p.startsWith("#")) {
			p = p.substring(1);
		}
		for (Channel c: plugin.getChannelManager().getPlayerChannels((Player) sender)) {
			if (c.getName().toLowerCase().startsWith(p)) {
				results.add("#" + c.getName());
			}
		}
		return results;
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (args.size() == 0) {
			showHelp(commandSender, label, args);
			return;
		}
		String name = args.pop();
		if (name.startsWith("#")) {
			name = name.substring(1);
		}
		Channel c = plugin.getChannelManager().getChannel(name);

		if (c == null) {
			throw new CommandError("Unknown channel: " + name);
		}

		List<Channel> mine = plugin.getChannelManager().getPlayerChannels((Player) commandSender);
		if (!mine.contains(c)) {
			if (!c.isListed()) {
				throw new CommandError("Unknown channel: " + name);
			}
			throw new CommandError("You are not in " + c.getName());
		}
		plugin.getChannelManager().leaveChannel((Player) commandSender, c);
		commandSender.sendMessage(ChatColor.DARK_GRAY + "You have left " + c.getName() + ". You are now chatting in " + plugin.getChannelManager().getFocusedChannel((Player) commandSender).getName());
	}
}
