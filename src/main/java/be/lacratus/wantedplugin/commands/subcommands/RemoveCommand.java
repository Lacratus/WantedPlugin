package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class RemoveCommand extends SubCommand {

    private WantedPlugin main;

    public RemoveCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "Remove";
    }

    @Override
    public String getPermission() {
        return "wanted.remove";
    }

    @Override
    public String getDescription() {
        return "Removes player from wanted list";
    }

    @Override
    public String getSyntax() {
        return "/wanted remove <Player>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage("Provide a name!");
            return;
        }
        Player target = main.getServer().getPlayerExact(args[1]);

        if (target == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.PlayerOffline")));
            return;
        }

        UUID uuid = target.getUniqueId();
        if (main.getWantedPlayers().containsKey(uuid)) {
            main.getWantedPlayers().get(uuid).setWantedLevel(0);
            main.getWantedPlayers().get(uuid).getBukkitTaskRemoveWanted().cancel();
            main.getWantedPlayers().remove(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.SuccesRemove")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
        }
    }
}
