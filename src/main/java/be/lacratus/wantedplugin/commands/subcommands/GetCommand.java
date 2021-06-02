package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class GetCommand extends SubCommand {

    private WantedPlugin main;

    public GetCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getPermission() {
        return "wanted.get";
    }

    @Override
    public String getDescription() {
        return "Get wantedlevel of a player";
    }

    @Override
    public String getSyntax() {
        return "/wanted get <Player>";
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bWanted&8] &fHet wantedlevel van &b"
                    + args[1] + "&f is &b" + main.getWantedPlayers().get(uuid).getWantedLevel()));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.PlayerNotWanted")));
        }
    }
}
