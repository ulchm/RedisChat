package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class ChannelCreateCommand extends BaseCommand {
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
		Channel c = new Channel();
		c.setName(args.pop());
		c.setListed(true);
		c.setOwnerId(((Player) commandSender).getUniqueId());
		c.setNameColor(ChatColor.YELLOW.toString());
		plugin.getChannelManager().saveChannel(c);
	}
}
