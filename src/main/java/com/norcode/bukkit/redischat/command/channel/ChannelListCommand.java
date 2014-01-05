package com.norcode.bukkit.redischat.command.channel;

import com.norcode.bukkit.redischat.Channel;
import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.chat.Text;
import com.norcode.bukkit.redischat.command.BaseCommand;
import com.norcode.bukkit.redischat.command.CommandError;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class ChannelListCommand extends BaseCommand {
	public ChannelListCommand(RedisChat plugin) {
		super(plugin, "list", new String[] {}, "redischat.command.channel.list", new String[] {});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		for (Channel c: plugin.getChannelManager().getAllChannels()) {
			if (c.isListed()) {
				if (channelMatches(c, args)) {
					int visibleCount = 0;
					for (String memberName: c.getMembers()) {
						if (((Player) commandSender).canSee(plugin.getServer().getPlayerExact(memberName))) {
							visibleCount++;
						}
					}
					plugin.send(
							commandSender,
							new Text("")
									.append(new Text("#" + c.getName()).setColor(c.getTextChatColor()))
									.append(" (" + visibleCount + ") ")
									.append((c.getDescription() != null && c.getDescription().length() > 0) ? " - " + c.getDescription() : "")
					);
				}
			}
		}
	}

	private boolean channelMatches(Channel c, LinkedList<String> args) {
		for (String a: args) {
			String al = a.toLowerCase();
			if (!c.getName().contains(al) &&
					(c.getDescription() == null || !c.getDescription().contains(al))) {
				return false;
			}
		}
		return true;
	}
}
