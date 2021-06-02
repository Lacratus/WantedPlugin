package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class EventCommand extends SubCommand {

    private WantedPlugin main;

    public EventCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "Event";
    }

    @Override
    public String getPermission() {
        return "wanted.event";
    }

    @Override
    public String getDescription() {
        return "Toggle Eventmodus";
    }

    @Override
    public String getSyntax() {
        return "/wanted event <Enable/Disable>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage("Provide a selector!");
            return;
        }
        if(!player.hasPermission("wanted.event")){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NoPermission")));
        }
        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable")) {
            main.setEventMode(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.EventmodusAan")));
        } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
            main.setEventMode(false);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.EventmodusUit")));
        }
    }
}
