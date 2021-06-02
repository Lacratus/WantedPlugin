package be.lacratus.wantedplugin.commands;

import be.lacratus.wantedplugin.WantedPlugin;
import be.lacratus.wantedplugin.commands.subcommands.*;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

    @Getter private ArrayList<SubCommand> subcommands = new ArrayList<>();

    public CommandManager(WantedPlugin main){
        subcommands.add(new ListCommand(main));
        subcommands.add(new GetCommand(main));
        subcommands.add(new EventCommand(main));
        subcommands.add(new SetCommand(main));
        subcommands.add(new ResetCommand(main));
        subcommands.add(new RemoveCommand(main));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player p = (Player) sender;

            if (args.length > 0){
                for (SubCommand sub: subcommands){
                    if (args[0].equalsIgnoreCase(sub.getName()) && p.hasPermission(sub.getPermission())){
                        sub.perform(p, args);
                    }
                }
            }else {
                p.sendMessage("--------------------------------");
                for (int i = 0; i < getSubcommands().size(); i++){
                    p.sendMessage(getSubcommands().get(i).getSyntax() + " - " + getSubcommands().get(i).getDescription());
                }
                p.sendMessage("--------------------------------");
            }

        }


        return true;
    }

}

