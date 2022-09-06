package com.github.tsukuba.ems_plugin;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class App extends JavaPlugin implements Listener {
    private String eventserver_url;
    private String frontserver_url;
    private String eventlistenserver_url;

    @Override
    public void onEnable() {
        getLogger().info("Spark, spark, spark!");
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        eventserver_url = config.getString("event_server");
        frontserver_url = config.getString("front_server");
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String setting_json = "{\"server\":\"" + eventserver_url + "\",\"id\":\"" + p.getUniqueId().toString() + "\"}";
        TextComponent url = new TextComponent(ChatColor.BLUE + "このURL");
        ComponentBuilder cb = new ComponentBuilder(
                "URL").bold(true).color(ChatColor.BLUE)
                .append(frontserver_url).color(ChatColor.GREEN).bold(false);
        url.setColor(ChatColor.AQUA);
        url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, cb.create()));
        url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, frontserver_url + "?setting=" +
                new String(
                        Base64
                                .getUrlEncoder()
                                .withoutPadding()
                                .encode(
                                        setting_json
                                                .getBytes(StandardCharsets.US_ASCII)),
                        StandardCharsets.US_ASCII)));
        url.addExtra(new TextComponent(ChatColor.WHITE + "に接続し感電マシーンの設定を行ってください"));
        p.spigot().sendMessage(url);
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
                    damaged.getUniqueId().toString(),
                    event.getDamage()));
            event_sender(damaged.getUniqueId().toString(), (int) event.getDamage());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            getLogger().info(String.format("Player:%s(id:%s)/death/dropped exp:%d/dropped items:%s",
                    ((Player) damaged).getPlayerListName(),
                    damaged.getUniqueId().toString(),
                    event.getDroppedExp(),
                    event.getDrops().toString()));
            event_sender(damaged.getUniqueId().toString(), 10);
        }
    }
}