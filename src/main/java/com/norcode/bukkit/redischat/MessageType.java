package com.norcode.bukkit.redischat;

public enum MessageType {
	BROADCAST("!"), CHANNEL("#"), PRIVATE("@");
	private String prefix;

	private MessageType(String prefix) {
		this.prefix = prefix;
	}

	public static MessageType fromPrefix(String prefix) {
		for (MessageType t: values()) {
			if (t.prefix.equals(prefix)) {
				return t;
			}
		}
		return null;
	}
}
