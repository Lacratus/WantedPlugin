package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ListCommand extends SubCommand {

    private WantedPlugin main;

    public ListCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "List";
    }

    @Override
    public String getPermission() {
        return "wanted.list";
    }

    @Override
    public String getDescription() {
        return "Give list of wanted players";
    }

    @Override
    public String getSyntax() {
        return "/wanted list";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (main.getWantedPlayers().entrySet().isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Message.EmptyList")));
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

    }
}
