package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChannelSetCommand extends BaseCommand {
	public ChannelSetCommand(RedisChat plugin) {
		super(plugin, "set", new String[] {}, "redischat.command.channel.set", new String[] {});
		registerSubcommand(new SetNameColorCommand(plugin));
	}

	public abstract static class SetCommand extends BaseCommand {
		public SetCommand(RedisChat plugin, String name, String[] aliases, String requiredPermission, String[] help) {
			super(plugin, name, aliases, requiredPermission, help);

		}

		@Override
		protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
			if (args.size() == 0) {
				showHelp(commandSender, label, args);
				return;
			}
			if (!(commandSender instanceof Player)) {
				throw new CommandError("This command is only available in-game.");
			}
			Channel c = plugin.getChannelManager().getFocusedChannel((Player) commandSender);
			if (c == null) {
				throw new CommandError("You are not currently in any channels?!?!");
			}
			if (!((Player) commandSender).getUniqueId().equals(c.getOwnerId()) && !((Player) commandSender).hasPermission("redischat.admin")) {
				throw new CommandError("You do not have permission to change that setting for this channel.");
			}
			onExecute((Player) commandSender, c, args);
		}

		protected abstract void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError;

		@Override
		protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
			Channel c = plugin.getChannelManager().getFocusedChannel((Player) sender);
			if (c != null) {
				return onTab((Player) sender, c, args);
			}
			return null;
		}
		protected abstract List<String> onTab(Player player, Channel channel, LinkedList<String> args);
	}

	public static class SetNameColorCommand extends SetCommand {

		public SetNameColorCommand(RedisChat plugin) {
			super(plugin, "namecolor", new String[] {}, "redischat.command.channel.set.namecolor",
					new String[] {"Sets the color of the channel name displayed as a prefix for each chat message"});
		}

		@Override
		protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
			if (args.size() == 0) {
				showHelp(player, "namecolor", args);
				return;
			}
			ChatColor clr = null;
			clr = ChatColor.valueOf(args.peek().toUpperCase());
			if (clr == null) {
				clr = ChatColor.getByChar(args.peek());
			}
			if (clr == null) {
				throw new CommandError("Unknown Color: " + args.peek());
			}
			channel.setNameColor(clr.toString());
			plugin.getChannelManager().saveChannel(channel);
			player.sendMessage("Channel 'name-color' has been set to: " + clr + clr.name());
		}

		@Override
		protected List<String> onTab(Player sender, Channel channel, LinkedList<String> args) {
			plugin.debug("onTabComplete nameColor w/ " + args.peek());
			List<String> results = new ArrayList<String>();
			if (args.size() == 1) {
				for (ChatColor c: ChatColor.values()) {
					if (c.name().toLowerCase().startsWith(args.peek().toLowerCase())) {
						results.add(c.name());
					}
				}
			}
			return results;
		}
	}
}
