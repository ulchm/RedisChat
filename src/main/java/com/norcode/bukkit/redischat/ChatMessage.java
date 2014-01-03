package com.norcode.bukkit.redischat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ChatMessage {

    private Player sender;
    private String senderLocation;
    private String message;
    private String format;
    private long sentAt;

    public ChatMessage(Player sender, String senderLocation, String message, String format, long sentAt) {
        this.sender = sender;
        this.senderLocation = senderLocation;
        this.message = message;
        this.format = format;
        this.sentAt = sentAt;
    }

    public Player getSender() {
        return sender;
    }

    public String getSenderLocation() {
        return senderLocation;
    }

    public String getMessage() {
        return message;
    }

    public String getFormat() {
        return format;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public void setSenderLocation(String senderLocation) {
        this.senderLocation = senderLocation;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }
}
