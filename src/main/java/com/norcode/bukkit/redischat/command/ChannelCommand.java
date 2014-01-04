package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.RedisChat;

public class ChannelCommand extends BaseCommand {

	public ChannelCommand(RedisChat plugin) {
		super(plugin, "channel", new String[] {"chan", "ch"}, "redischat.command.channel", null);
		registerSubcommand(new ChannelCreateCommand(plugin));
		registerSubcommand(new ChannelInfoCommand(plugin));
		registerSubcommand(new ChannelSetCommand(plugin));
	}
}
