package com.norcode.bukkit.redischat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Channel {

	private transient HashSet<String> members = new HashSet<String>();
	private long createdAt;
	private long lastJoinedAt;
	private boolean autoJoin = false;
	private boolean noDelete = false;
	private UUID ownerId;
	private Set<UUID> opIdSet = new HashSet<UUID>();
	private String name;
	private String description = "";
	private String nameColor;
	private String textColor = ChatColor.GRAY.toString();
	private String password;
	private String joinPermission;
	private String chatPermission;
	private int radius = -1;
	private boolean listed = true;

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameColor() {
		return nameColor;
	}

	public void setNameColor(String nameColor) {
		this.nameColor = nameColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public HashSet<String> getMembers() {
		return members;
	}

	public boolean isListed() {
		return listed;
	}

	public void setListed(boolean listed) {
		this.listed = listed;
	}

	public String getJoinPermission() {
		return joinPermission;
	}

	public void setJoinPermission(String joinPermission) {
		this.joinPermission = joinPermission;
	}

	public String getChatPermission() {
		return chatPermission;
	}

	public void setChatPermission(String chatPermission) {
		this.chatPermission = chatPermission;
	}

	public boolean isMember(String name) {
		return members.contains(name);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public Set<UUID> getOpIdSet() {
		return opIdSet;
	}

	public void setOpIdSet(Set<UUID> opIdSet) {
		this.opIdSet = opIdSet;
	}

	public boolean isOp(Player player) {
		return opIdSet.contains(player.getUniqueId());
	}

	public void opPlayer(Player p) {
		opIdSet.add(p.getUniqueId());
	}

	public void deopPlayer(Player p) {
		opIdSet.remove(p.getUniqueId());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public long getLastJoinedAt() {
		return lastJoinedAt;
	}

	public void setLastJoinedAt(long lastJoinedAt) {
		this.lastJoinedAt = lastJoinedAt;
	}

	public boolean isNoDelete() {
		return noDelete;
	}

	public void setNoDelete(boolean noDelete) {
		this.noDelete = noDelete;
	}

	public boolean isAutoJoin() {
		return autoJoin;
	}

	public void setAutoJoin(boolean autoJoin) {
		this.autoJoin = autoJoin;
	}

	public ChatColor getNameChatColor() {
		if (nameColor.length() == 0) {
			return null;
		} else {
			return ChatColor.getByChar(nameColor.charAt(1));
		}
	}

	public ChatColor getTextChatColor() {
		if (textColor.length() == 0) {
			return null;
		} else {
			return ChatColor.getByChar(textColor.charAt(1));
		}
	}
}
