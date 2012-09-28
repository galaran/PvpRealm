package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.DoOrNotify;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PvpRealmCommandExecutor implements CommandExecutor {

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
                    GUtils.sendTranslated(sender, "reload.ok");
                } else {
                    GUtils.sendTranslated(sender, "reload.error");
                }
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("enter")) {
                if (Settings.pvpwEnabled) {
                    Player player = DoOrNotify.getPlayer(args[1], true, sender);
                    if (player != null) {
                        om.getPvpPlayer(player).enterPvpRealm();
                    }
                } else {
                    GUtils.sendTranslated(sender, "world.disabled");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("return")) {
                if (Settings.pvpwEnabled) {
                    Player player = DoOrNotify.getPlayer(args[1], true, sender);
                    if (player != null) {
                        om.getPvpPlayer(player).leavePvpRealm();
                    }
                } else {
                    GUtils.sendTranslated(sender, "world.disabled");
                }
                return true;
            }
        }
        return false;
    }
}
