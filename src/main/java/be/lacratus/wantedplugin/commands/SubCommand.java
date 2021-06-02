package be.lacratus.wantedplugin.commands;

import org.bukkit.entity.Player;

public abstract class SubCommand {

    // name of the subcommand
    public abstract String getName();

    // Permission u need to use command
    public abstract String getPermission();

    // ex. "Gives all people who are wanted"
    public abstract String getDescription();

    // How to use command ex. /wanted set <player> <time>
    public abstract String getSyntax();

    // code for the subcommand
    public abstract void perform(Player player, String args[]);


}
