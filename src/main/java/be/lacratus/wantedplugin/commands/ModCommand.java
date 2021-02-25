package be.lacratus.wantedplugin.commands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ModCommand implements CommandExecutor {

    private WantedPlugin main;
    Player target;
    UUID uuid;

    public ModCommand(WantedPlugin wantedPlugin) {
        this.main = wantedPlugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check of sender een speler is
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        // Als de lengte korter of langer is dan 2, return.
        if (args.length != 2) {
            sendHelpMessage(player);
            return false;
        }

        target = main.getServer().getPlayerExact(args[1]);

        // Kijken of target op de server is
        if (target == null) {
            player.sendMessage("Deze speler bestaat niet");
            return false;
        }

        uuid = target.getUniqueId();
        DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
        // Target jailen of unjailen
        if (args[0].equalsIgnoreCase("unjail")) {
            ddgPlayer.setJailed(false);
        } else if (args[0].equalsIgnoreCase("Jail")) {
            ddgPlayer.setJailed(true);
        }
        return false;
    }

    public void sendHelpMessage(Player player) {
        player.sendMessage("/mod - Sends helpmessage \n" +
                "/mod jail <Naam> - jail player \n" +
                "/mod unjail <Naam> - Unjail player");
    }
}
