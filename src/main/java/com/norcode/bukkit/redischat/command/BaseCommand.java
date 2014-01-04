package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.RedisChat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BaseCommand implements TabExecutor {
	protected RedisChat plugin;
	private String name;
	private String[] aliases;
	private String requiredPermission;
	private String[] help;
	private Map<String, BaseCommand> subcommands = new HashMap<String, BaseCommand>();

	public BaseCommand(RedisChat plugin, String name, String[] aliases, String requiredPermission, String[] help) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
		this.requiredPermission = requiredPermission;
		this.help = help;
		this.subcommands = subcommands;
	}

	protected void registerSubcommand(BaseCommand cmd) {
		subcommands.put(cmd.name, cmd);
	}

	@Override
	public final boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		try {
			onCommand(commandSender, label, new LinkedList<String>(Arrays.asList(args)));
		} catch (CommandError error) {
			plugin.send(commandSender, error.getMessage());
		}
		return true;
	}

	Map.Entry<String, BaseCommand> getSubcommand(CommandSender sender, String arg) {
		for (Map.Entry<String, BaseCommand> sc : subcommands.entrySet()) {
			if (sc.getValue().name.equalsIgnoreCase(arg))
				return sc;
		}
		for (Map.Entry<String, BaseCommand> sc : subcommands.entrySet()) {
			for (String a : sc.getValue().aliases) {
				if (a.equalsIgnoreCase(arg))
					return sc;
			}
		}

		return null;
	}

	void onCommand(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (requiredPermission == null || commandSender.hasPermission(requiredPermission)) {
			if (args.size() > 0) {
				String sub = args.peek().toLowerCase();
				BaseCommand subCmd = null;
				try {
					subCmd = getSubcommand(commandSender, args.peek().toLowerCase()).getValue();
				} catch (NullPointerException ex) {
				}
				if (subCmd != null) {
					subCmd.onCommand(commandSender, args.pop(), args);
					return;
				}
			}
			onExecute(commandSender, label, args);
		}
	}

	protected void showHelp(CommandSender sender, String label, LinkedList<String> args) {
		if (help.length > 0) {
			plugin.send(sender, help);
		}
		if (subcommands.size() > 0) {
			plugin.send(sender, "Sub-Commands: " + StringUtils.join(filterByPermission(sender, subcommands).keySet(), ", "));
		}
	}

	protected Map<String, BaseCommand> filterByPermission(CommandSender sender, Map<String, BaseCommand> subcommands) {
		Map<String, BaseCommand> filtered = new HashMap<String, BaseCommand>();
		for (Map.Entry<String, BaseCommand> entry : subcommands.entrySet()) {
			if (entry.getValue().requiredPermission == null || sender.hasPermission(entry.getValue().requiredPermission)) {
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		return filtered;
	}

	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {

	}

	@Override
	public final List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return onTabComplete(commandSender, s, new LinkedList<String>(Arrays.asList(strings)));
	}

	List<String> onTabComplete(CommandSender sender, String label, LinkedList<String> args) {
		if (requiredPermission == null || sender.hasPermission(requiredPermission)) {
			if (args.size() == 0) {
				return null;
			}
			Map<String, BaseCommand> subs = filterByPermission(sender, subcommands);
			String partial = args.peek().toLowerCase();
			BaseCommand sub;
			try {
				sub = getSubcommand(sender, partial).getValue();
			} catch (NullPointerException ex) {
				sub = null;
			}
			if (sub != null) {
				args.pop();
				return sub.onTabComplete(sender, partial, args);
			} else {
				List<String> results = new ArrayList<String>();
				for (BaseCommand sc : filterByPermission(sender, subcommands).values()) {
					if (sc.name.toLowerCase().startsWith(partial)) {
						results.add(sc.name);
					} else {
						for (String a : sc.aliases) {
							if (a.toLowerCase().startsWith(partial)) {
								results.add(a);
								break;
							}
						}
					}
				}
				List<String> localCmdResults = onTab(sender, args);
				if (localCmdResults != null) {
					results.addAll(localCmdResults);
				}
				return results;
			}
		}
		return null;
	}

	protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
		return null;
	}
}
