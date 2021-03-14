package be.lacratus.wantedplugin;

import be.lacratus.wantedplugin.commands.ModCommand;
import be.lacratus.wantedplugin.commands.WantedCommand;
import be.lacratus.wantedplugin.data.StoredDataHandler;
import be.lacratus.wantedplugin.listeners.OnDisconnectListener;
import be.lacratus.wantedplugin.listeners.OnJoinListener;
import be.lacratus.wantedplugin.listeners.OnKillListener;
import be.lacratus.wantedplugin.listeners.OnMoveListener;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Getter
@Setter
public final class WantedPlugin extends JavaPlugin {


    // Database
    private HikariDataSource hikari;
    private static Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    // API's
    private WorldGuardPlugin worldGuardPlugin;
    private WorldEdit worldEditPlugin;

    // Flags
    public static StateFlag CAMERA_FLAG;

    private StoredDataHandler storedDataHandler;


    private Map<UUID, DDGPlayer> wantedPlayers;
    private Map<UUID, DDGPlayer> onlinePlayers;

    private boolean eventMode;

    @Override
    public void onLoad() {
        // ... do your own plugin things, etc
        System.out.println("Plugin loading");
        FlagRegistry registry = getWorldGuard().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StateFlag flag = new StateFlag("camera-flag", true);
            registry.register(flag);
            CAMERA_FLAG = flag; // only set our field if there was no error
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("camera-flag");
            if (existing instanceof StateFlag) {
                CAMERA_FLAG = (StateFlag) existing;
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
                System.out.println("Some shit went really wrong");
            }
        }
    }

    @Override
    public void onEnable() {
        System.out.println("Plugin Enabled");
        //Config - Databank creation
        this.getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.host = this.getConfig().getString("Host");
        this.port = this.getConfig().getInt("Port");
        this.database = this.getConfig().getString("Database");
        this.username = this.getConfig().getString("Username");
        this.password = this.getConfig().getString("Password");
        // Hikari configuration
        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(10);
        hikari.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
        hikari.setUsername(this.username);
        hikari.setPassword(this.password);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Initialise api's
        worldGuardPlugin = getWorldGuard();
        worldEditPlugin = getWorldEdit();

        this.storedDataHandler = new StoredDataHandler(this);
        this.wantedPlayers = new HashMap<>();
        this.onlinePlayers = new HashMap<>();
        this.eventMode = false;

        //Commands
        getCommand("Wanted").setExecutor(new WantedCommand(this));
        getCommand("Mod").setExecutor(new ModCommand(this));

        //Listeners
        Bukkit.getPluginManager().registerEvents(new OnJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnDisconnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnKillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnMoveListener(this), this);

        //Save Playerinfo to databank every ... minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, storedDataHandler.savePlayerData(), 20L * 1800, 20L * 1800);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        storedDataHandler.savePlayerData().run();

        // Sluit hikari
        hikari.close();
    }

    public Connection openConnection() {
        try {
            connection = hikari.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    public void warn(DDGPlayer ddgPlayer) {
        int x = (int) ddgPlayer.getPlayer().getLocation().getX();
        int y = (int) ddgPlayer.getPlayer().getLocation().getZ();
        int z = (int) ddgPlayer.getPlayer().getLocation().getY();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WAARSCHUWING!\n" + "WANTED: ").append(ddgPlayer.getPlayer().getDisplayName()).append("\n").append("X: ").append(x)
                .append(" Y: ").append(y).append(" Z: ").append(z);
        for (Player justitie : Bukkit.getOnlinePlayers()) {
            if(ddgPlayer.getWantedLevel() == 20 && (justitie.hasPermission("wanted.leger") || justitie.hasPermission("wanted.agent"))) {
                justitie.sendMessage(String.valueOf(stringBuilder));
            } else if (justitie.hasPermission("wanted.agent")) {
                justitie.sendMessage(String.valueOf(stringBuilder));
            }
        }
    }

    public void runRemoveWantedLevel(DDGPlayer ddgPlayer) {
        if (ddgPlayer.getBukkitTaskRemoveWanted() != null) {
            ddgPlayer.getBukkitTaskRemoveWanted().cancel();
        }
        Player player = ddgPlayer.getPlayer();
        player.sendMessage("Je wantedlevel is " + ddgPlayer.getWantedLevel() + ", niet uitloggen!");

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            int wantedLevel = ddgPlayer.getWantedLevel();
            wantedLevel -= 1;
            ddgPlayer.setWantedLevel(wantedLevel);
            if (wantedLevel <= 0) {
                getWantedPlayers().remove(ddgPlayer.getUuid());
                ddgPlayer.getBukkitTaskRemoveWanted().cancel();
                player.sendMessage("Je bent niet meer wanted, maar pasop, log niet uit als je in een gevaarlijke situatie zit.");
                return;
            }

            player.sendMessage("Je wantedlevel is " + wantedLevel + ", niet uitloggen! Je wantedlevel zal elke minuut verminderen.");
        }, 20L*60, 20L*60);
        ddgPlayer.setBukkitTaskRemoveWanted(bukkitTask);
    }

    public void updateLists(DDGPlayer ddgPlayer) {
        UUID uuid = ddgPlayer.getUuid();
        if (getOnlinePlayers().containsKey(uuid)) {
            getOnlinePlayers().put(uuid, ddgPlayer);
        }
        if (getWantedPlayers().containsKey(uuid)) {
            getWantedPlayers().put(uuid, ddgPlayer);
        }
    }

    public void runRemoveKills(DDGPlayer ddgPlayer) {
        if (ddgPlayer.getBukkitTaskRemoveKillTimer() != null) {
            ddgPlayer.getBukkitTaskRemoveKillTimer().cancel();
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            ddgPlayer.setMadeKill(false);
            ddgPlayer.setMadeKillInLastDay(false);
            ddgPlayer.setRemoveKillsTimer(0L);
            updateLists(ddgPlayer);
        }, 20L * (ddgPlayer.getRemoveKillsTimer() - System.currentTimeMillis() / 1000));
        ddgPlayer.setBukkitTaskRemoveKillTimer(bukkitTask);
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    private WorldEdit getWorldEdit() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        // WorldGuard may not be loaded
        if (!(plugin instanceof WorldEdit)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldEdit) plugin;
    }

    public boolean inCameraRegion(Player player) {
        LocalPlayer localPlayer = getWorldGuardPlugin().wrapPlayer(player);
        Vector playerVector = localPlayer.getPosition();
        RegionManager regionManager = getWorldGuardPlugin().getRegionManager(player.getWorld());
        ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(playerVector);

        for (ProtectedRegion region : applicableRegionSet) {
            if (region.contains(playerVector) && region.getFlag(CAMERA_FLAG) == StateFlag.State.ALLOW) {
                return true;
            }
        }
        return false;
    }
}
