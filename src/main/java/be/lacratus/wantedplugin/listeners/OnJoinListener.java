package be.lacratus.wantedplugin.listeners;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.data.StoredDataHandler;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class OnJoinListener implements Listener {

    private WantedPlugin main;
    private StoredDataHandler storedDataHandler;

    public OnJoinListener(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        storedDataHandler.loadData(uuid).whenComplete((ddgPlayer, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            main.getOnlinePlayers().put(uuid, ddgPlayer);
            if (ddgPlayer.isMadeKillInLastDay() || ddgPlayer.isMadeKill()) {
                main.runRemoveKills(ddgPlayer);
            }
        });

    }


}
