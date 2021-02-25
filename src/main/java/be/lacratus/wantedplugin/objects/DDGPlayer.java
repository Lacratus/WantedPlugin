package be.lacratus.wantedplugin.objects;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Getter @Setter
public class DDGPlayer {

    Player player;
    private UUID uuid;
    private int wantedLevel;
    private String username;
    private long removeKillsTimer;
    private boolean madeKill;
    private boolean madeKillInLastDay;
    private BukkitTask bukkitTaskRemoveWanted;
    private BukkitTask bukkitTaskRemoveKillTimer;
    private boolean isJailed;

    public DDGPlayer(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.wantedLevel = 0;
        this.username = Bukkit.getPlayer(uuid).getDisplayName();
        this.removeKillsTimer = 0L;
        this.madeKill = false;
        this.madeKillInLastDay = false;
        this.isJailed = false;
    }
    public DDGPlayer(Player player, int wantedLevel, Long removeKillsTimer, boolean madeKill, boolean madeKillInLastDay, boolean isJailed) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.wantedLevel = wantedLevel;
        this.username = Bukkit.getPlayer(uuid).getDisplayName();
        this.removeKillsTimer = removeKillsTimer + System.currentTimeMillis() / 1000;
        this.madeKill = madeKill;
        this.madeKillInLastDay = madeKillInLastDay;
        this.isJailed = isJailed;
    }
}
