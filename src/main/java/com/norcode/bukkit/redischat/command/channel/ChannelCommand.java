package com.norcode.bukkit.redischat.command.channel;

import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.command.BaseCommand;

public class ChannelCommand extends BaseCommand {

	public ChannelCommand(RedisChat plugin) {
		super(plugin, "channel", new String[] {"chan"}, "redischat.command.channel", null);
		plugin.getCommand("channel").setExecutor(this);
		registerSubcommand(new ChannelCreateCommand(plugin));
		registerSubcommand(new ChannelInfoCommand(plugin));
		registerSubcommand(new ChannelSetCommand(plugin));
		registerSubcommand(new ChannelJoinCommand(plugin));
		registerSubcommand(new ChannelLeaveCommand(plugin));
		registerSubcommand(new ChannelOpCommand(plugin));
		registerSubcommand(new ChannelListCommand(plugin));

	}
}
