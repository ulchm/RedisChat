package com.norcode.bukkit.redischat.command;

import com.norcode.bukkit.redischat.ChatMessage;
import com.norcode.bukkit.redischat.MetaKeys;
import com.norcode.bukkit.redischat.RedisChat;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;

public class ReplyCommand extends BaseCommand {
	public ReplyCommand(RedisChat plugin) {
		super(plugin, "reply", new String[] {"r"}, "redischat.command.reply", new String[] {"Reply to the last person you sent or recieved a PM to."});
		plugin.getCommand("reply").setExecutor(this);
	}

	@Override
	protected void onExecute(CommandSender sender, String label, LinkedList<String> args) throws CommandError {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			throw new CommandError("This command is only available in-game.");
		}

		if (args.size() == 0) {
			showHelp(sender, label, args);
			return;
		}
		Player target = null;

		if (!player.hasMetadata(MetaKeys.PM_REPLY_TO)) {
			throw new CommandError("You do not have an ongoing conversation.");
		}
		String targetName = player.getMetadata(MetaKeys.PM_REPLY_TO).get(0).asString();
		target = Bukkit.getPlayerExact(targetName);
		if (target == null) {
			throw new CommandError(targetName + " has gone offline.");
		}

		final String dest = "@" + target.getName();
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
}
