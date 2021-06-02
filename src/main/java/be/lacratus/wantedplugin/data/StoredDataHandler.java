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
import java.util.concurrent.CompletionException;

public class StoredDataHandler {

    private WantedPlugin main;

    public StoredDataHandler(WantedPlugin main) {
        this.main = main;
    }

    public Runnable saveData(DDGPlayer data) {
        // Data gets updated to database
        return () -> {
            try (Connection connection = main.openConnection()) {
                updatePlayer(data, connection);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };
    }


    public CompletableFuture<DDGPlayer> loadData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // First login makes row in table
            try (Connection connection = main.openConnection();
                 PreparedStatement ps = connection.prepareStatement("SELECT * FROM ddgplayer WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                // Nieuwe speler wordt aangemaakt in database
                if (!rs.next()) {
                    PreparedStatement ps2 = connection.prepareStatement("INSERT INTO ddgplayer(uuid,wantedLevel,timeOfLastKill,madeKill,madeKillInLastDay) VALUES(?,DEFAULT,DEFAULT,DEFAULT,DEFAULT)");
                    ps2.setString(1, uuid.toString());
                    ps2.executeUpdate();
                    ps2.close();
                    rs.close();
                    Player player = Bukkit.getPlayer(uuid);
                    return new DDGPlayer(player);

                    // Speler bestaat al in database
                } else {
                    int wantedlevel = rs.getInt("wantedLevel");
                    long timeOfLastkill = rs.getInt("timeOfLastKill") - System.currentTimeMillis() / 1000;
                    boolean madeKill;
                    madeKill = rs.getInt("MadeKill") != 0;
                    boolean madeKillInLastDay;
                    madeKillInLastDay = rs.getInt("MadeKillInLastDay") != 0;
                    rs.close();
                    Player player = Bukkit.getPlayer(uuid);
                    return new DDGPlayer(player, wantedlevel, timeOfLastkill, madeKill, madeKillInLastDay);
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
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
        try (PreparedStatement ps = connection.prepareStatement("UPDATE ddgplayer SET WantedLevel = ?, TimeOfLastKill = ?, MadeKill = ?, MadeKillInLastDay = ? WHERE Uuid= ? ")) {
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
            ps.setInt(1, data.getWantedLevel());
            ps.setLong(2, data.getRemoveKillsTimer());
            ps.setInt(3, madeKill);
            ps.setInt(4, madeKillInLastDay);
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
