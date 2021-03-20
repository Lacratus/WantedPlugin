package be.lacratus.wantedplugin.commands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class WantedCommand implements CommandExecutor {

    private WantedPlugin main;
    Player target;
    UUID uuid;
    int firstKill;
    int secondKill;
    int thirdKill;

    public WantedCommand(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
        firstKill = main.getConfig().getInt("FirstKill");
        secondKill = main.getConfig().getInt("SecondKill");
        thirdKill = main.getConfig().getInt("ThirdKill");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check of sender een speler is
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        // Permissions vallen in te stellen, dit is een placeholder
        if (!player.hasPermission("Korpschef") || !player.hasPermission("Generaal")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
            return false;
        }
        // Als aantal argumenten 0 of meer dan 3 is, return.
        if (args.length == 0 || args.length > 3) {
            sendHelpMessage(player);
            return true;
        }

        if (args.length == 1) {
            // Geef lijst van alle wanted personen
            if (args[0].equalsIgnoreCase("list")) {

                if(main.getWantedPlayers().entrySet().isEmpty()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.EmptyList")));
                    return true;
                }
                StringBuilder list = new StringBuilder();
                list.append("Wantedlist: \n");
                for (Map.Entry<UUID, DDGPlayer> entry : main.getWantedPlayers().entrySet()) {
                    list.append(entry.getValue().getUsername())
                            .append(": Wantedlevel ")
                            .append(entry.getValue().getWantedLevel())
                            .append("\n");
                }
                player.sendMessage(String.valueOf(list));

            } else {
                sendHelpMessage(player);
            }
            return true;
        }


        if (args.length == 2) {
            // Event modus aan/uit zetten, dit zorgt ervoor dat als iemand vermoord wordt deze niet wanted wordt gezet
            if (args[0].equalsIgnoreCase("event")) {
                if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable")) {
                    main.setEventMode(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.EventmodusAan")));
                } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
                    main.setEventMode(false);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.EventmodusUit")));
                } else {
                    sendHelpMessage(player);
                }
                return true;
            }

            target = main.getServer().getPlayerExact(args[1]);
            // Verwijder Wantedlevels van een speler
            try {
                if (args[0].equalsIgnoreCase("remove")) {
                    uuid = target.getUniqueId();
                    if (main.getWantedPlayers().containsKey(uuid)) {
                        main.getWantedPlayers().get(uuid).setWantedLevel(0);
                        main.getWantedPlayers().get(uuid).getBukkitTaskRemoveWanted().cancel();
                        main.getWantedPlayers().remove(uuid);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.SuccesRemove")));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
                    }
                    // Geef terug of speler wanted is en hoelang
                } else if (args[0].equalsIgnoreCase("get")) {
                    uuid = target.getUniqueId();
                    if (main.getWantedPlayers().containsKey(uuid)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bWanted&8] &fHet wantedlevel van &b"
                                + args[1] + "&f is &b" + main.getWantedPlayers().get(uuid).getWantedLevel()));

                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
                    }
                } else {
                    sendHelpMessage(player);
                }
                return true;
            }catch (NullPointerException exception){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.PlayerOffline")));
                return true;
            }
        }

        // Kijken of speler online is
        if (main.getServer().getPlayerExact(args[1]) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.PlayerOffline")));
            return true;
        }


        uuid = target.getUniqueId();
        // Zet iemand wanted
        if (args[0].equalsIgnoreCase("set")) {
            try {
                int wantedLevel = Integer.parseInt(args[2]);
                if (wantedLevel > thirdKill || wantedLevel < 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NotNegative")));
                    return false;
                }
                DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
                ddgPlayer.setWantedLevel(wantedLevel);
                main.getWantedPlayers().put(uuid, ddgPlayer);
                if (wantedLevel >= secondKill) {
                    main.warn(ddgPlayer);
                }
                main.runRemoveWantedLevel(ddgPlayer);
                main.updateLists(ddgPlayer);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.LevelNeedNumbers")));
            }
        } else {
            sendHelpMessage(player);
        }
        return false;
    }

    public void sendHelpMessage(Player player) {
        player.sendMessage("/Wanted - Sends helpmessage \n" +
                "/Wanted list - Give every wanted person \n" +
                "/Wanted remove <Player> - Remove all wantedlevels of player \n" +
                "/Wanted get <Player> - Get wantedlevel of player\n" +
                "/Wanted set <Player> <Number> - set wantedlevel of player, maximum of 20!\n" +
                "/Wanted event <Enable|Disable>");
    }
}
