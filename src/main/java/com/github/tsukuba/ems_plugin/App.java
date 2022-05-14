package com.github.tsukuba.ems_plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.http.HttpClient;

public class App extends JavaPlugin implements Listener {
    private String eventserver_url;
    private String frontserver_url;

    @Override
    public void onEnable() {
        getLogger().info("Spark, spark, spark!");
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        eventserver_url = config.getString("event_Server");
        frontserver_url = config.getString("front_Server");
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event){
        Player p = event.getPlayer();
        String setting_json="{\"server\":\""+eventserver_url+"\",\"id\":"+p.getUniqueId().toString()+"\"}";
        p.sendMessage(frontserver_url+"?setting="+
            new String(
                Base64
                .getUrlEncoder()
                .withoutPadding()
                .encode(
                    setting_json
                    .getBytes(StandardCharsets.US_ASCII)
                ),
                StandardCharsets.US_ASCII
            )+ "に接続し感電マシーンの設定を行ってください");
    }

    private void event_sender(String player_id,int modifier){
        String setting_json="{\"who\":\""+player_id+"\",\"modifier\":"+Integer.toString(modifier)+"\"}";
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damaged = event.getEntity();
        if(damaged instanceof Player){
            getLogger().info(String.format("Player:%s(id:%s)/damage/damage amount:%e",
                                ((Player)damaged).getPlayerListName(),
                                damaged.getUniqueId().toString(),
                                event.getDamage()));
            event_sender(damaged.getUniqueId().toString(),(int)event.getDamage());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            getLogger().info(String.format("Player:%s(id:%s)/death/dropped exp:%d/dropped items:%s",
                                ((Player)damaged).getPlayerListName(),
                                damaged.getUniqueId().toString(),
                                event.getDroppedExp(),
                                event.getDrops().toString()));
            event_sender(damaged.getUniqueId().toString(),10);
        }
    }
}