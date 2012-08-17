package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PvpRealmCommandExecutor implements CommandExecutor {

    public static final String PLAYER_NOT_FOUND = "No player with name: ";

    public static final Map<String, SubCommandExecutor> subCommands = new HashMap<String, SubCommandExecutor>();

    static {
        subCommands.put("bpoint", new BattlePointCommands());
        subCommands.put("kit", new KitCommands());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            SubCommandExecutor subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                return subCommand.onCommand(sender, label, newArgs);
            }
        }

        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setentry")) {
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    Location newEntryLoc = senderPlayer.getLocation();
                    if (newEntryLoc.getWorld().equals(Config.getPvpWorld())) {
                        Config.entryLoc = newEntryLoc;
                        GUtils.sendMessage(sender, "Entry location has been set: "  + GUtils.locToStringWorldXYZ(newEntryLoc));
                    } else {
                        GUtils.sendMessage(sender, "You must be in the Pvp World");
                    }
                } else {
                    GUtils.sendMessage(sender, "This command can be executed only by a player");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setreturn")) {
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    Location newReturnLoc = senderPlayer.getLocation();
                    if (!newReturnLoc.getWorld().equals(Config.getPvpWorld())) {
                        Config.defaultReturnLoc = newReturnLoc;
                        GUtils.sendMessage(sender, "Default return location has been set: " + GUtils.locToStringWorldXYZ(newReturnLoc));
                    } else {
                        GUtils.sendMessage(sender, "You must be out of the Pvp World");
                    }
                } else {
                    GUtils.sendMessage(sender, "This command can be executed only by a player");
                }
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("enter")) {
                String playerName = args[1];
                Player player = Bukkit.getServer().getPlayerExact(playerName);
                if (player != null) {
                    om.getPvpPlayer(player).enterPvpRealm();
                } else {
                    GUtils.sendMessage(sender, PLAYER_NOT_FOUND + playerName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("return")) {
                String playerName = args[1];
                Player player = Bukkit.getServer().getPlayerExact(playerName);
                if (player != null) {
                    om.getPvpPlayer(player).leavePvpRealm();
                } else {
                    GUtils.sendMessage(sender, PLAYER_NOT_FOUND + playerName);
                }
                return true;
            }
        }

        return false;
    }
}
