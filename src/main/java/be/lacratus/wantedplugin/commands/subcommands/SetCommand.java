package be.lacratus.wantedplugin.commands.subcommands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.SubCommand;
import be.lacratus.wantedplugin.objects.DDGPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class SetCommand extends SubCommand {

    private WantedPlugin main;

    public SetCommand(WantedPlugin main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "Set";
    }

    @Override
    public String getPermission() {
        return "wanted.set";
    }

    @Override
    public String getDescription() {
        return "Give player a wantedlevel";
    }

    @Override
    public String getSyntax() {
        return "/wanted set <Player> <Number>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage("Provide a name!");
            return;
        }

        if (args.length == 2) {
            player.sendMessage("Provide a number!");
            return;
        }
        int secondKill = main.getConfig().getInt("Wanted.SecondKill");
        Player target = main.getServer().getPlayerExact(args[1]);
        UUID uuid = target.getUniqueId();
        try {
            int wantedLevel = Integer.parseInt(args[2]);
            if (wantedLevel > main.getConfig().getInt("Wanted.MaxLevel") || wantedLevel < 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.NotNegative")));
                return;
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getString("Message.LevelNeedNumbers")));
        }
    }
}
