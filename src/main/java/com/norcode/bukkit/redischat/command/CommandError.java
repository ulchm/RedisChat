package com.norcode.bukkit.redischat.command;

public class CommandError extends Exception {
	public CommandError() {
	}

	public CommandError(String message) {
		super(message);
	}

	public CommandError(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandError(Throwable cause) {
		super(cause);
	}

	public CommandError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
