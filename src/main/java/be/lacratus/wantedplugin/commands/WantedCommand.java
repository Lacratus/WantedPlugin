package be.lacratus.wantedplugin.commands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WantedCommand implements CommandExecutor, TabCompleter {

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


        // Als aantal argumenten 0 of meer dan 3 is, return.
        if (args.length == 0 || args.length > 3) {
            sendHelpMessage(player);
            return true;
        }

        if (args.length == 1) {
            // Geef lijst van alle wanted personen
            if (args[0].equalsIgnoreCase("list")) {
                if(!player.hasPermission("wanted.list")){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                    return false;
                }
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
                if(!player.hasPermission("wanted.event")){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                    return false;
                }
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
                    if(!player.hasPermission("wanted.remove")){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                        return false;
                    }
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
                    if(!player.hasPermission("wanted.get")){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                        return false;
                    }
                    uuid = target.getUniqueId();
                    if (main.getWantedPlayers().containsKey(uuid)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bWanted&8] &fHet wantedlevel van &b"
                                + args[1] + "&f is &b" + main.getWantedPlayers().get(uuid).getWantedLevel()));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
                    }
                } else if (args[0].equalsIgnoreCase("reset")) {
                    if(!player.hasPermission("wanted.reset")){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                        return false;
                    }
                    uuid = target.getUniqueId();
                    if (main.getOnlinePlayers().containsKey(uuid)) {
                        try {
                            main.getOnlinePlayers().get(uuid).setWantedLevel(0);
                            main.getOnlinePlayers().get(uuid).getBukkitTaskRemoveWanted().cancel();
                            main.getOnlinePlayers().get(uuid).setMadeKill(false);
                            main.getOnlinePlayers().get(uuid).setMadeKillInLastDay(false);
                            main.getWantedPlayers() .remove(uuid);
                        }catch (Exception ignored){
                        }finally {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.SuccesReset")));
                        }
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

        target = main.getServer().getPlayerExact(args[1]);
        uuid = target.getUniqueId();
        // Zet iemand wanted
        if (args[0].equalsIgnoreCase("set")) {
            if(!player.hasPermission("wanted.set")){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
                return false;
            }
            try {
                int wantedLevel = Integer.parseInt(args[2]);
                if (wantedLevel > main.getConfig().getInt("MaxLevel") || wantedLevel < 0) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1){
            List<String> commands = new ArrayList<>();
            commands.add("list");
            commands.add("get");
            commands.add("set");
            commands.add("remove");
            commands.add("reset");
            commands.add("event");

            return commands;
        } else if(args[0].contains("event")){
            List<String> parameters = new ArrayList<>();
            parameters.add("Enable");
            parameters.add("Disable");

            return parameters;
        }
        return null;
    }

    public void sendHelpMessage(Player player) {
        player.sendMessage("/Wanted - Sends helpmessage \n" +
                "/Wanted list - Give every wanted person \n" +
                "/Wanted remove <Player> - Remove all wantedlevels of player \n" +
                "/Wanted reset <Player> - Remove all wantedlevels of player and remove 24/48 hour timers. \n" +
                "/Wanted get <Player> - Get wantedlevel of player\n" +
                "/Wanted set <Player> <Number> - set wantedlevel of player, maximum of 20!\n" +
                "/Wanted event <Enable|Disable>");
    }


}
