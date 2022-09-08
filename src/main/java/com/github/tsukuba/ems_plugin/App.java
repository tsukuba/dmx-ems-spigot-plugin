package com.github.tsukuba.ems_plugin;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class App extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Spark, spark, spark!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void event_sender(String player_id, int modifier) {
        String setting_json = "{\"who\":\"" + player_id + "\",\"modifier\":\"" + Integer.toString(modifier) + "\"}";
        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://eventserver:3000/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(setting_json))
                .build();
        try {
            client.sendAsync(req,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).thenAccept((ret) -> {
                        getLogger().info(String.format("Event send status code:%d", ret.statusCode()));
                    });
        } catch (Exception e) {
            getLogger().info("Some error occurred in event sending...");
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            getLogger().info(String.format("Player:%s(id:%s)/damage/damage amount:%e",
                    ((Player) damaged).getPlayerListName(),
                    damaged.getName().toString(),
                    event.getDamage()));
            event_sender(damaged.getName(), (int) event.getDamage());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            getLogger().info(String.format("Player:%s(id:%s)/death/dropped exp:%d/dropped items:%s",
                    ((Player) damaged).getPlayerListName(),
                    damaged.getName(),
                    event.getDroppedExp(),
                    event.getDrops().toString()));
            event_sender(damaged.getName(), 10);
        }
    }
}