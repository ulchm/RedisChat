package com.norcode.bukkit.redischat.command.chat;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.redischat.MetaKeys;
import com.norcode.bukkit.redischat.RedisChat;
import com.norcode.bukkit.redischat.command.CommandError;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SetPMNotificationCommand extends ChatSetCommand.ChatSetSubCommand {

	public SetPMNotificationCommand(RedisChat plugin) {
		super(plugin, "pmnotification", new String[] {}, "redischat.command.set.pmnotification", new String[] {});
	}

	@Override
	public void onExecute(Player p, LinkedList<String> args) throws CommandError {
		Sound snd = null;
		if (args.size() > 0) {
			snd = Sound.valueOf(args.peek().toUpperCase());
			if (snd == null) {
				throw new CommandError("Unknown Sound: " + args.peek());
			}
		}
		ConfigurationSection cfg = PlayerID.getPlayerData(plugin.getName(), p);
		cfg.set(MetaKeys.PM_NOTIFICATION, snd == null ? "" : snd.name());
		PlayerID.savePlayerData(plugin.getName(), p, cfg);
		p.setMetadata(MetaKeys.PM_NOTIFICATION, new FixedMetadataValue(plugin, snd));
		String info = "cleared.";
		if (snd != null) {
			info = "set to " + snd.name() + ".";
			p.playSound(p.getLocation(), snd, 10, 1);
		}
		plugin.send(p, "Your incoming private messages notification has been " + info);
	}

	@Override
	public List<String> onTab(Player p, LinkedList<String> args) {
		List<String> results = new ArrayList<String>();
		for (Sound s: Sound.values()) {
			if (s.name().toLowerCase().startsWith(args.peek().toLowerCase())) {
				results.add(s.name());
			}
		}
		return results;
	}
}
