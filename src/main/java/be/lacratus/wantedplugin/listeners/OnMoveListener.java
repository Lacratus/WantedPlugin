package be.lacratus.wantedplugin.listeners;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class OnMoveListener implements Listener {

    WantedPlugin main;

    public OnMoveListener(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
    }


    @EventHandler
    public void on(PlayerMoveEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
    }
}
