package com.norcode.bukkit.redischat.chat;

import net.minecraft.server.v1_7_R1.EnumClickAction;

public enum ClickAction {
	OPEN_URL(EnumClickAction.OPEN_URL),
	OPEN_FILE(EnumClickAction.OPEN_FILE),
	RUN_COMMAND(EnumClickAction.RUN_COMMAND),
	SUGGEST_COMMAND(EnumClickAction.SUGGEST_COMMAND);

	private EnumClickAction clickAction;

	ClickAction(EnumClickAction action) {
		this.clickAction = action;
	}

	public EnumClickAction getNMS() {
		return this.clickAction;
	}
}
