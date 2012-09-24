package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private final PvpRealm plugin;

    public PvpRealmCommandExecutor(PvpRealm plugin) {
        this.plugin = plugin;
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
            if (args[0].equalsIgnoreCase("reload")) {
                if (plugin.loadConfig()) {
                    GUtils.sendMessage(sender, "Configuration reloaded", ChatColor.GREEN);
                } else {
                    GUtils.sendMessage(sender, "There is error reloading config, see server console", ChatColor.RED);
                }
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("enter")) {
                if (Config.pvpWorldEnabled) {
                    String playerName = args[1];
                    Player player = Bukkit.getServer().getPlayerExact(playerName);
                    if (player != null) {
                        om.getPvpPlayer(player).enterPvpRealm();
                    } else {
                        GUtils.sendMessage(sender, PLAYER_NOT_FOUND + playerName);
                    }
                } else {
                    GUtils.sendMessage(sender, "Pvp World disabled", ChatColor.RED);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("return")) {
                if (Config.pvpWorldEnabled) {
                    String playerName = args[1];
                    Player player = Bukkit.getServer().getPlayerExact(playerName);
                    if (player != null) {
                        om.getPvpPlayer(player).leavePvpRealm();
                    } else {
                        GUtils.sendMessage(sender, PLAYER_NOT_FOUND + playerName);
                    }
                } else {
                    GUtils.sendMessage(sender, "Pvp World disabled", ChatColor.RED);
                }
                return true;
            }
        }
        return false;
    }
}
