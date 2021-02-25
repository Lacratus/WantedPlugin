package be.lacratus.wantedplugin.data;


import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StoredDataHandler {

    private WantedPlugin main;

    public StoredDataHandler(WantedPlugin main) {
        this.main = main;
    }

    public void saveData(DDGPlayer data) {
        // Data gets updated to database
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try (Connection connection = main.openConnection()) {
                updatePlayer(data, connection);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }


    public CompletableFuture<DDGPlayer> loadData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // First login makes row in table
            try (Connection connection = main.openConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT COUNT(uuid) FROM ddgplayer WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                rs.next();
                // Nieuwe speler wordt aangemaakt in database
                if (rs.getInt(1) == 0) {
                    PreparedStatement ps2 = connection.prepareStatement("INSERT INTO ddgplayer(uuid,wantedLevel,timeOfLastKill,madeKill,madeKillInLastDay,isJailed) VALUES(?,DEFAULT,DEFAULT,DEFAULT,DEFAULT,DEFAULT)");
                    ps2.setString(1, uuid.toString());
                    ps2.executeUpdate();
                    ps2.close();
                    rs.close();
                    Player player = Bukkit.getPlayer(uuid);
                    return new DDGPlayer(player);

                    // Speler bestaat al in database
                } else {
                    PreparedStatement ps3 = connection.prepareStatement("SELECT * FROM ddgplayer WHERE uuid = ?");
                    ps3.setString(1, uuid.toString());
                    ResultSet rs2 = ps3.executeQuery();
                    rs2.next();
                    int wantedlevel = rs2.getInt("wantedLevel");
                    long timeOfLastkill = rs2.getInt("timeOfLastKill") - System.currentTimeMillis() / 1000;
                    boolean madeKill;
                    madeKill = rs2.getInt("MadeKill") != 0;
                    boolean madeKillInLastDay;
                    madeKillInLastDay = rs2.getInt("MadeKillInLastDay") != 0;
                    boolean isJailed;
                    isJailed = rs2.getInt("IsJailed") != 0;
                    rs2.close();
                    ps3.close();
                    rs.close();
                    Player player = Bukkit.getPlayer(uuid);
                    return new DDGPlayer(player, wantedlevel, timeOfLastkill, madeKill, madeKillInLastDay, isJailed);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // Updaten van ALLE spelerdata
    public Runnable savePlayerData() {
        return () -> {
            try (Connection connection = main.openConnection()) {
                Map<UUID, DDGPlayer> onlinePlayers = main.getOnlinePlayers();
                for (Map.Entry<UUID, DDGPlayer> entry : onlinePlayers.entrySet()) {
                    updatePlayer(entry.getValue(), connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }

    // Updaten van spelerdata
    public void updatePlayer(DDGPlayer data, Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE ddgplayer SET WantedLevel = ?, TimeOfLastKill = ?, MadeKill = ?, MadeKillInLastDay = ?, IsJailed = ? WHERE Uuid= ? ")) {
            int madeKill;
            if (data.isMadeKill()) {
                madeKill = 1;
            } else {
                madeKill = 0;
            }

            int madeKillInLastDay;
            if (data.isMadeKillInLastDay()) {
                madeKillInLastDay = 1;
            } else {
                madeKillInLastDay = 0;
            }

            int isJailed;
            if (data.isJailed()) {
                isJailed = 1;
            } else {
                isJailed = 0;
            }
            ps.setInt(1, data.getWantedLevel());
            ps.setLong(2, data.getRemoveKillsTimer());
            ps.setInt(3, madeKill);
            ps.setInt(4, madeKillInLastDay);
            System.out.println(isJailed);
            ps.setInt(5, isJailed);
            ps.setString(6, data.getUuid().toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (main.getServer().getPlayerExact(data.getUsername()) == null) {
            main.getOnlinePlayers().remove(data.getUuid());
        }
        ;
    }
}
