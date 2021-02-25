package be.lacratus.wantedplugin.listeners;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.data.StoredDataHandler;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class OnJoinListener implements Listener {

    WantedPlugin main;
    private StoredDataHandler storedDataHandler;

    public OnJoinListener(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        storedDataHandler.loadData(uuid).thenAccept(ddgPlayer -> {
            main.getOnlinePlayers().put(uuid, ddgPlayer);
            if(ddgPlayer.isMadeKillInLastDay() || ddgPlayer.isMadeKill()) {
                main.runRemoveKills(ddgPlayer);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

    }


}
