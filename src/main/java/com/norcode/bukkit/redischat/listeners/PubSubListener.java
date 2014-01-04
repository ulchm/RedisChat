package com.norcode.bukkit.redischat.listeners;

import com.norcode.bukkit.redischat.ChatMessage;
import com.norcode.bukkit.redischat.RedisChat;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class PubSubListener extends JedisPubSub implements Runnable {

	private RedisChat plugin;
	private Jedis jedis;
	private Gson gson = new Gson();

	public PubSubListener(RedisChat plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		ChatMessage msg = gson.fromJson(message, ChatMessage.class);
		msg.setDestination(channel.substring(5));
		plugin.getChatRenderer().getMessageQueue().add(msg);
	}

	@Override
	public void run() {
		jedis = plugin.getJedisPool().getResource();
		jedis.psubscribe(this, "chat:*");
		plugin.getJedisPool().returnResource(jedis);
	}

	public synchronized void stopRunning() {
		this.unsubscribe();
	}

	@Override
	public void onPSubscribe(String s, int i) {
	}

	@Override
	public void onPUnsubscribe(String s, int i) {
	}

	@Override
	public void onSubscribe(String s, int i) {
	}

	@Override
	public void onUnsubscribe(String s, int i) {
	}

	@Override
	public void onMessage(String s, String s2) {
	}
}
