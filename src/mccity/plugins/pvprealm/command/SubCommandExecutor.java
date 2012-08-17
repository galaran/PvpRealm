package mccity.plugins.pvprealm.command;

import org.bukkit.command.CommandSender;

public interface SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args);

}
