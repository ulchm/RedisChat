package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ChannelOpCommand extends BaseCommand {

	private OpListCommand listCommand;
	public ChannelOpCommand(RedisChat plugin) {
		super(plugin, "operators", new String[] {"ops", "opers"}, "redischat.command.channel.operators", null);
		listCommand = new OpListCommand(plugin);
		registerSubcommand(new OpAddCommand(plugin));
		registerSubcommand(new OpRemoveCommand(plugin));
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (args.size() == 0) {
			listCommand.onExecute(commandSender, "list", args);
			return;
		}

	}

	public static class OpListCommand extends BaseCommand {

		public OpListCommand(RedisChat plugin) {
			super(plugin, "list", new String[]{}, "redischat.command.channel.operators", new String[]{});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Channel c = plugin.getChannelManager().getFocusedChannel((Player) commandSender);
			String msg = "Operators for #" + c.getName() + ": ";
			for (UUID uuid: c.getOpIdSet()) {
				msg += PlayerID.getPlayerName(uuid) + ", ";
			}
			if (msg.endsWith(", ")) {
				msg = msg.substring(0, msg.length()-2);
			}
			commandSender.sendMessage(msg);
		}
	}

	public static class OpAddCommand extends BaseCommand {

		public OpAddCommand(RedisChat plugin) {
			super(plugin, "add", new String[]{}, "redischat.command.channel.operators.add", new String[]{});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Channel c = plugin.getChannelManager().getFocusedChannel((Player) commandSender);
			if (!c.isOp((Player) commandSender) && !((Player) commandSender).getUniqueId().equals(c.getOwnerId())) {
				throw new CommandError("You don't have permission to edit the operator list for this channel.");
			}
			List<Player> matches = new ArrayList<Player>();
			for (Player p: plugin.getServer().getOnlinePlayers()) {
				if (p.getName().startsWith(args.peek().toLowerCase()) && ((Player) commandSender).canSee(p)) {
					matches.add(p);
				}
			}
			if (matches.size() == 0) {
				throw new CommandError("Unknown player: " + args.peek());
			} else if (matches.size() > 1) {
				Player match = null;
				for (Player p: matches) {
					if (p.getName().equalsIgnoreCase(args.peek())) {
						match = p;
						break;
					}
				}
				if (match == null) {
					throw new CommandError("'" + args.peek() + "' matches more than 1 player.");
				}
				matches.clear();
				matches.add(match);
			}
			Player target = matches.get(0);
			c.getOpIdSet().add(target.getUniqueId());
			plugin.send(commandSender, target.getName() + " has been added to the operator list for #" + c.getName());
			plugin.getChannelManager().saveChannel(c);
		}
	}

	public static class OpRemoveCommand extends BaseCommand {

		public OpRemoveCommand(RedisChat plugin) {
			super(plugin, "remove", new String[]{}, "redischat.command.channel.operators.remove", new String[]{});
		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			Channel c = plugin.getChannelManager().getFocusedChannel((Player) commandSender);
			if (!c.isOp((Player) commandSender) && !((Player) commandSender).getUniqueId().equals(c.getOwnerId())) {
				throw new CommandError("You don't have permission to edit the operator list for this channel.");
			}
			List<Player> matches = new ArrayList<Player>();
			for (Player p: plugin.getServer().getOnlinePlayers()) {
				if (p.getName().startsWith(args.peek().toLowerCase()) && ((Player) commandSender).canSee(p)) {
					matches.add(p);
				}
			}
			if (matches.size() == 0) {
				throw new CommandError("Unknown player: " + args.peek());
			} else if (matches.size() > 1) {
				Player match = null;
				for (Player p: matches) {
					if (p.getName().equalsIgnoreCase(args.peek())) {
						match = p;
						break;
					}
				}
				if (match == null) {
					throw new CommandError("'" + args.peek() + "' matches more than 1 player.");
				}
				matches.clear();
				matches.add(match);
			}
			Player target = matches.get(0);
			if (!c.getOpIdSet().contains(target.getUniqueId())) {
				throw new CommandError(target.getName() + " is not an operator in #" + c.getName());
			}
			c.getOpIdSet().remove(target.getUniqueId());
			plugin.send(commandSender, target.getName() + " has been removed from the operator list for #" + c.getName());
			plugin.getChannelManager().saveChannel(c);
		}
	}


}
