package com.norcode.bukkit.redischat.command.channel;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.command.BaseCommand;
import com.norcode.bukkit.redischat.command.CommandError;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChannelJoinCommand extends BaseCommand {
	public ChannelJoinCommand(RedisChat plugin) {
		super(plugin, "join", new String[] {"j"}, "redischat.command.channel.join", new String[]{});
	}

	@Override
	protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
		List<String> results = new ArrayList<String>();
		String p = args.peek().toLowerCase();
		if (p.startsWith("#")) {
			p = p.substring(1);
		}
		for (Channel c: plugin.getChannelManager().getAllChannels()) {
			if (c.isListed() && c.getName().toLowerCase().startsWith(p)) {
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
		if (c.getJoinPermission() != null && !commandSender.hasPermission(c.getJoinPermission())) {
			if (!c.isListed()) {
				throw new CommandError("Unknown channel: " + name);
			} else {
				throw new CommandError("You do not have permission to join " + name);
			}
		}
		String passwd = null;
		if (c.getPassword() != null) {
			if (args.size() == 0) {
				throw new CommandError("This channel requires a password.");
			}
			passwd = args.pop();
			if (!passwd.equals(c.getPassword())) {
				throw new CommandError("Incorrect Channel Password");
			}
		}
		plugin.getChannelManager().joinChannel((Player) commandSender, c, passwd);
		commandSender.sendMessage(ChatColor.DARK_GRAY + "You are now chatting in " + c.getName());
	}
}
