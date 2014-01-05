package com.norcode.bukkit.redischat.command.chat;

import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.command.BaseCommand;
import com.norcode.bukkit.redischat.command.CommandError;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class ChatSetCommand extends BaseCommand {

	public ChatSetCommand(RedisChat plugin) {
		super(plugin, "set", new String[]{}, "redischat.command.chat.set", new String[]{});
		registerSubcommand(new SetPMNotificationCommand(plugin));
	}

	public static abstract class ChatSetSubCommand extends BaseCommand {

		public ChatSetSubCommand(RedisChat plugin, String name, String[] aliases, String requiredPermission, String[] help) {
			super(plugin, name, aliases, requiredPermission, help);
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (!(commandSender instanceof  Player)) {
				throw new CommandError("This command is only available in-game.");
			}
			Player p = (Player) commandSender;
			this.onExecute(p, args);
		}

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			return this.onTab((Player) sender, args);
		}

		public abstract void onExecute(Player p, LinkedList<String> args) throws CommandError;
		public abstract List<String> onTab(Player p, LinkedList<String> args);
	}



}
