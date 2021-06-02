package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ResetCommand extends SubCommand {

    private WantedPlugin main;

    public ResetCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "Reset";
    }

    @Override
    public String getPermission() {
        return "wanted.reset";
    }

    @Override
    public String getDescription() {
        return "Reset wantedstats of a player";
    }

    @Override
    public String getSyntax() {
        return "/wanted Reset <Player>";
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
        if (main.getOnlinePlayers().containsKey(uuid)) {
            main.getOnlinePlayers().get(uuid).setWantedLevel(0);
            main.getOnlinePlayers().get(uuid).getBukkitTaskRemoveWanted().cancel();
            main.getOnlinePlayers().get(uuid).setMadeKill(false);
            main.getOnlinePlayers().get(uuid).setMadeKillInLastDay(false);
            main.getWantedPlayers().remove(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.SuccesReset")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
        }
    }
}
