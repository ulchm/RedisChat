package com.norcode.bukkit.redischat;

public class ChatMessage {

    private String sender;
    private String message;
	private String destination;
    private long sentAt;

    public ChatMessage(String sender, String destination, String message, long sentAt) {
        this.sender = sender;
		this.destination = destination;
        this.message = message;
        this.sentAt = sentAt;
    }

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

	public String toString() {
		return "ChatMessage{sender=" + sender + ", destination=" + destination + ", sentAt=" + sentAt + ", message=" + message + "}";
	}
}
