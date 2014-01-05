package com.norcode.bukkit.redischat.command.chat;

import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.command.BaseCommand;

public class ChatCommand extends BaseCommand {

	public ChatCommand(RedisChat plugin) {
		super(plugin, "chat", new String[] {}, "redischat.command.chat", new String[] {});
		registerSubcommand(new ChatSetCommand(plugin));
		plugin.getCommand("chat").setExecutor(this);
	}
}
