package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BattlePointCommands implements SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                StringBuilder points = new StringBuilder();
                for (BattlePoint curBattlePoint : om.getBattlePoints()) {
                    points.append(curBattlePoint.getName());
                    points.append(' ');
                }
                GUtils.sendMessage(sender, "Battle points: " + points.toString());
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    String pointName = args[1];
                    Location playerLoc = senderPlayer.getLocation();
                    if (playerLoc.getWorld().equals(Config.pvpWorld)) {
                        BattlePoint newPoint = new BattlePoint(senderPlayer.getLocation(), pointName);
                        om.addBattlePoint(newPoint);
                        GUtils.sendMessage(senderPlayer, "Point " + pointName + " has been added");
                    } else {
                        GUtils.sendMessage(senderPlayer, "You must be in the Pvp World");
                    }
                } else {
                    GUtils.sendMessage(sender, "This command can be executed only by a player");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeBattlePoint(args[1])) {
                    GUtils.sendMessage(sender, "Battle point " + args[1] + " has been removed");
                } else {
                    GUtils.sendMessage(sender, "No such battle point: " + args[1]);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                BattlePoint battlePoint = om.getBattlePoint(args[1]);
                if (battlePoint != null) {
                    GUtils.sendMessage(sender, "Battle point " + battlePoint.getName() + " - " +
                            GUtils.locToStringWorldXYZ(battlePoint.getLoc()));
                } else {
                    GUtils.sendMessage(sender, "Battle point " + args[1] + " not exists");
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("tp")) {
                String playerName = args[1];
                String prefix = args[2];

                Player player = Bukkit.getServer().getPlayerExact(playerName);
                if (player != null) {
                    PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                    if (!pvpPlayer.tpToBattlePoint(prefix)) {
                        GUtils.sendMessage(sender, "There is no battle point matched " + ChatColor.GRAY + prefix);
                    }
                } else {
                    GUtils.sendMessage(sender, PvpRealmCommandExecutor.PLAYER_NOT_FOUND + playerName);
                }
                return true;
            }
        }

        return false;
    }
}
