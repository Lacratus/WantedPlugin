package be.lacratus.wantedplugin.listeners;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.data.StoredDataHandler;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class OnDisconnectListener implements Listener {

    private WantedPlugin main;
    private StoredDataHandler storedDataHandler;

    public OnDisconnectListener(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
        // Speler jailen bij uitloggen wanneer wanted
        if (main.getWantedPlayers().containsKey(uuid)) {
            ddgPlayer.setWantedLevel(0);
            ddgPlayer.getBukkitTaskRemoveWanted().cancel();
            ddgPlayer.getBukkitTaskRemoveKillTimer().cancel();
            main.getWantedPlayers().remove(uuid);
            main.updateLists(ddgPlayer);
            // Melding versturen naar justitie
            for (Player justitie : Bukkit.getOnlinePlayers()) {
                if (justitie.hasPermission("wanted.warnArmy") || justitie.hasPermission("wanted.warnCops")) {
                    justitie.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8[&bWanted&8] De speler " + player.getDisplayName() + " is uitgelogd. Hij is in de cel geplaatst"));
                }
            }
            Bukkit.getScheduler().runTaskAsynchronously(main, storedDataHandler.saveData(ddgPlayer));
        }
    }
}
