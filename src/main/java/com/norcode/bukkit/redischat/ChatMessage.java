package com.norcode.bukkit.redischat;

import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.Player;

public class ChatMessage {

    private Player sender;
    private Location senderLocation;
    private String message;
    private String format;
    private long sentAt;

    public ChatMessage(Player sender, Location senderLocation, String message, String format, long sentAt) {
        this.sender = sender;
        this.senderLocation = senderLocation;
        this.message = message;
        this.format = format;
        this.sentAt = sentAt;
    }

    public Player getSender() {
        return sender;
    }

    public Location getSenderLocation() {
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
}
