package com.norcode.bukkit.redischat;

import com.norcode.bukkit.redischat.listeners.PubSubListener;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisChat extends JavaPlugin implements Listener {

    private JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379, 0);
    private PubSubListener pubSubListener = new PubSubListener(this);
    private Gson gson = new Gson();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Thread listenerThread = new Thread(pubSubListener);
        listenerThread.start();
    }

    @Override
    public void onDisable() {
        pubSubListener.stopRunning();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @EventHandler(ignoreCancelled = true)
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        ChatMessage chatMessage = new ChatMessage(event.getPlayer(), event.getPlayer().getLocation(), event.getMessage(), event.getFormat(), System.currentTimeMillis());
        Jedis jedis = jedisPool.getResource();
        jedis.publish("chat:all", gson.toJson(chatMessage));
        jedisPool.returnResource(jedis);
        event.setCancelled(true);
    }
}