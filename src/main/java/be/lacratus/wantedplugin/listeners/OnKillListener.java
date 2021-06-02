package be.lacratus.wantedplugin.listeners;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class OnKillListener implements Listener {

    private WantedPlugin main;

    public OnKillListener(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if(main.isEventMode()) {
            return;
        }
            Player killer = event.getEntity().getKiller();
            UUID uuid = killer.getUniqueId();
            if (main.getOnlinePlayers().containsKey(uuid)) {
                DDGPlayer player = main.getOnlinePlayers().get(uuid);

                if (main.inCameraRegion(killer)) {
                    setWantedLevel(player, main.getConfig().getInt("Wanted.ThirdKill"));
                } else if (player.isMadeKill()) {
                    setWantedLevel(player, main.getConfig().getInt("Wanted.SecondKill"));
                } else if (player.isMadeKillInLastDay()) {
                    setWantedLevel(player, main.getConfig().getInt("Wanted.ThirdKill"));
                } else {
                    setWantedLevel(player, main.getConfig().getInt("Wanted.FirstKill"));
                }

                if (player.getBukkitTaskRemoveKillTimer() != null) {
                    player.getBukkitTaskRemoveKillTimer().cancel();
                }
                main.getWantedPlayers().put(uuid, player);
                main.runRemoveKills(player);
                main.runRemoveWantedLevel(player);
                main.updateLists(player);
            }
    }

    public void setWantedLevel(DDGPlayer player,int wantedlevel) {
        player.setWantedLevel(wantedlevel);
        if (wantedlevel >= main.getConfig().getInt("Wanted.SecondKill")) {
            player.setMadeKill(false);
            player.setMadeKillInLastDay(true);
            main.warn(player);
            player.setRemoveKillsTimer(System.currentTimeMillis() / 1000 + 172800);
        } else {
            player.setMadeKill(true);
            player.setRemoveKillsTimer(System.currentTimeMillis() / 1000 + 86400);
        }
    }
}
