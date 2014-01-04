package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.ChatMessage;
import com.norcode.bukkit.redischat.MetaKeys;
import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.chat.Text;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PrivateMessageCommand extends BaseCommand {
	public PrivateMessageCommand(RedisChat plugin) {
		super(plugin, "msg", new String[] {"whisper", "tell", "pm"}, "redischat.command.msg", null);
		plugin.getCommand("msg").setExecutor(this);
	}

	@Override
	protected void onExecute(CommandSender sender, String label, LinkedList<String> args) throws CommandError {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (args.size() == 0) {
			showHelp(sender, label, args);
			return;
		}
		String playerName = args.peek().toLowerCase();
		List<Player> matches = new ArrayList<Player>();
		for (Player p: plugin.getServer().getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(playerName)) {
				if ((player == null || player.canSee(p))) {
					matches.add(p);
				}
			}
		}
		Player target = null;
		if (matches.size() == 0) {
			throw new CommandError("Unknown Player: " + args.peek());
		} else if (matches.size() > 1) {
			for (Player p: matches) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					target = p;
					break;
				}
			}
			if (target == null) {
				throw new CommandError("'" + args.peek() + "' matches more than one player.");
			}
		} else {
			target = matches.get(0);
		}
		if (target.getName().equals(sender.getName())) {
			plugin.send(target, new Text(" * ").setColor(ChatColor.WHITE)
					                .append(new Text("You mumble to yourself, but you can't quite understand what you're trying to say.").setColor(ChatColor.GOLD).setItalic(true)));
			return;
		}
		if (player != null) {
			player.setMetadata(MetaKeys.PM_REPLY_TO, new FixedMetadataValue(plugin, target.getName()));
		}
		final String dest = "@" + target.getName();
		args.pop();
		final ChatMessage msg = new ChatMessage(sender.getName(), dest, StringUtils.join(args, " "), System.currentTimeMillis());
		new BukkitRunnable() {
			@Override
			public void run() {
				Jedis j = plugin.getJedisPool().getResource();
				j.publish("chat:" + dest, plugin.getGson().toJson(msg));
				plugin.getJedisPool().returnResource(j);
			}
		}.runTaskAsynchronously(plugin);
	}

	@Override
	List<String> onTabComplete(CommandSender sender, String label, LinkedList<String> args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		List<String> results = new ArrayList<String>();
		if (args.size() == 1) {
			for (Player p: plugin.getServer().getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args.peek().toLowerCase())) {
					if ((player == null || player.canSee(p)) && !p.getName().equals(sender.getName())) {
						results.add(p.getName());
					}
				}
			}
		}
		return results;
	}



}
