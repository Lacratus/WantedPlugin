package be.lacratus.wantedplugin.commands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
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

    public WantedCommand(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
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
            System.out.println("You don't have permission to use this command");
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
                StringBuilder list = new StringBuilder();
                for (Map.Entry<UUID, DDGPlayer> entry : main.getWantedPlayers().entrySet()) {
                    list.append("Wantedlist:")
                            .append(entry.getValue().getUsername())
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
                    player.sendMessage("De Eventmodus is nu AAN");
                } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
                    main.setEventMode(false);
                    player.sendMessage("De Eventmodus is nu UIT");
                } else {
                    sendHelpMessage(player);
                }
                return true;
            }
            target = main.getServer().getPlayerExact(args[1]);

            // Kijken of speler online is
            if (target == null) {
                player.sendMessage("Deze speler is niet online");
                return true;
            }

            uuid = target.getUniqueId();
            // Verwijder Wantedlevels van een speler
            if (args[0].equalsIgnoreCase("remove")) {
                if (main.getWantedPlayers().containsKey(uuid)) {
                    main.getWantedPlayers().get(uuid).setWantedLevel(0);
                    main.getWantedPlayers().get(uuid).getBukkitTaskRemoveWanted().cancel();
                    main.getWantedPlayers().remove(uuid);
                    player.sendMessage("Deze speler is succesvol verwijdert uit de wantedlijst");
                } else {
                    player.sendMessage("Deze speler is niet wanted");
                }
                // Geef terug of speler wanted is en hoelang
            } else if (args[0].equalsIgnoreCase("get")) {
                if (main.getWantedPlayers().containsKey(uuid)) {
                    player.sendMessage("Het wantedlevel van " + args[1] + " is " + main.getWantedPlayers().get(uuid).getWantedLevel());
                } else {
                    player.sendMessage("Deze speler is niet wanted.");
                }
            } else {
                sendHelpMessage(player);
            }
            return true;
        }

        // Kijken of speler online is
        if (main.getServer().getPlayerExact(args[1]) == null) {
            player.sendMessage("Deze speler is niet online");
            return true;
        }


        uuid = target.getUniqueId();
        // Zet iemand wanted
        if (args[0].equalsIgnoreCase("set")) {
            try {
                int wantedLevel = Integer.parseInt(args[2]);
                if (wantedLevel > 20 || wantedLevel < 0) {
                    player.sendMessage("wantedlevel mag niet negatief zijn en niet meer dan 20");
                    return false;
                }
                DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
                ddgPlayer.setWantedLevel(wantedLevel);
                main.getWantedPlayers().put(uuid, ddgPlayer);
                if (wantedLevel == 20) {
                    main.warn(ddgPlayer);
                } else if (wantedLevel >= 10) {
                    main.warn(ddgPlayer);
                }
                main.runRemoveWantedLevel(ddgPlayer);
                main.updateLists(ddgPlayer);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                player.sendMessage("You can't set a player wanted with letters");
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
                "/Wanted set <Player> <Number> - set wantedlevel of player, maximum of 20\n" +
                "/Wanted event <Enable|Disable>");
        for (Map.Entry<UUID, DDGPlayer> entry : main.getOnlinePlayers().entrySet()) {
            System.out.println(entry.getValue());
        }
    }
}
