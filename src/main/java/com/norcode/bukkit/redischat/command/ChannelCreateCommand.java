package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.regex.Pattern;

public class ChannelCreateCommand extends BaseCommand {
	private Pattern chanNamePattern = Pattern.compile("^[\\w\\d]{1,8}$");

	public ChannelCreateCommand(RedisChat plugin) {
		super(plugin, "create", new String[] {"new"}, "redischat.command.channel.create", new String[]{});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (args.size() == 0) {
			showHelp(commandSender, label, args);
			return;
		}
		if (plugin.getChannelManager().channelExists(args.peek())) {
			throw new CommandError("Channel '" +  args.peek() + "' already exists!");
		}
		if (!isValidChannelName(args.peek())) {
			throw new CommandError(args.peek() + "is not a valid channel name.");
		}
		Channel c = new Channel();
		c.setName(args.pop());
		c.setListed(true);
		c.setOwnerId(((Player) commandSender).getUniqueId());
		c.setNameColor(ChatColor.YELLOW.toString());
		plugin.getChannelManager().saveChannel(c);
		if (plugin.getChannelManager().joinChannel((Player) commandSender, c, null)) {
			commandSender.sendMessage(ChatColor.DARK_GRAY + "You are now chatting in #" + c.getName());
		}
	}

	public boolean isValidChannelName(String s) {
		if (chanNamePattern.matcher(s).matches()) {
			return true;
		}
		return false;
	}
}
