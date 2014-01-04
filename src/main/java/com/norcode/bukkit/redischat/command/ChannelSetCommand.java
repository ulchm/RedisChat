package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import com.sun.javaws.exceptions.InvalidArgumentException;
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

    public static class SetTextColorCommand extends SetCommand {

        public SetTextColorCommand(RedisChat plugin) {
            super(plugin, "textcolor", new String[] {}, "redischat.command.channel.set.textcolor",
                    new String[] {"Sets the color of the channel text displayed on player messages."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            if (args.size() == 0) {
                showHelp(player, "textcolor", args);
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
            player.sendMessage("Channel 'text-color' has been set to: " + clr + clr.name());
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
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

    public static class SetPassword extends SetCommand {
        public SetPassword(RedisChat plugin) {
            super(plugin, "password", new String[] {}, "redischat.command.channel.set.password",
                    new String[] {"Sets a password needed to join the channel."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            if (args.size() == 0) {
                channel.setPassword(null);
                plugin.getChannelManager().saveChannel(channel);
                return;
            }
            String password = args.peek();
            channel.setPassword(password);
            plugin.getChannelManager().saveChannel(channel);
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
            return null;
        }
    }

    public static class SetJoinPermission extends SetCommand {
        public SetJoinPermission(RedisChat plugin) {
            super(plugin, "joinpermission", new String[] {}, "redischat.admin",
                    new String[] {"Admin only - Sets a permission required to join the channel."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            if (args.size() == 0) {
                channel.setJoinPermission(null);
                plugin.getChannelManager().saveChannel(channel);
                return;
            }
            String permission = args.peek();
            channel.setJoinPermission(permission);
            plugin.getChannelManager().saveChannel(channel);
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
            return null;
        }
    }

    public static class SetChatPermission extends SetCommand {
        public SetChatPermission(RedisChat plugin) {
            super(plugin, "chatpermissions", new String[] {}, "redischat.admin",
                    new String[] {"Admin only - Sets a permission required to chat in the channel."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            if (args.size() == 0) {
                channel.setChatPermission(null);
                plugin.getChannelManager().saveChannel(channel);
                return;
            }
            String permission = args.peek();
            channel.setChatPermission(permission);
            plugin.getChannelManager().saveChannel(channel);
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
            return null;
        }
    }

    public static class SetRadius extends SetCommand {
        public SetRadius(RedisChat plugin) {
            super(plugin, "radius", new String[] {}, "redischat.command.set.radius",
                    new String[] {"Sets the radius that the channel will be effective."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            Integer radius = -1;
            if (args.size() == 0) {
                channel.setRadius(radius);
                plugin.getChannelManager().saveChannel(channel);
                return;
            }
            try {
                radius = Integer.parseInt(args.peek());
                if (radius < 1) {
                    throw new CommandError("Radius can't be less then 1");
                }
            }
            catch (IllegalArgumentException e){
                    throw new CommandError("Radius can't be less then 1.");
            }
            channel.setRadius(radius);
            plugin.getChannelManager().saveChannel(channel);
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
            return null;
        }
    }

    public static class SetListed extends SetCommand {
        public SetListed(RedisChat plugin) {
            super(plugin, "listed", new String[] {}, "redischat.command.set.listed",
                    new String[] {"Determines whether the channel will be visible in the list."});
        }

        @Override
        protected void onExecute(Player player, Channel channel, LinkedList<String> args) throws CommandError {
            if (args.size() == 0) {
                showHelp(player, "listed", args);
                return;
            }
            Boolean listed = null;
            try {
                listed = Boolean.parseBoolean(args.pop());
            } catch (IllegalArgumentException e) {
                throw new CommandError("You must enter true or false.");
            }
            channel.setListed(listed);
            plugin.getChannelManager().saveChannel(channel);
        }

        @Override
        protected List<String> onTab(Player player, Channel channel, LinkedList<String> args) {
            return null;
        }
    }
}
