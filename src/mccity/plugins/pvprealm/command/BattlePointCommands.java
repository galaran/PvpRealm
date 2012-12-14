package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.LocUtils;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import me.galaran.bukkitutils.pvprealm.text.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BattlePointCommands implements SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                Messaging.send(sender, "bp.list",
                        StringUtils.join(om.listBattlePoints(), ChatColor.DARK_PURPLE, ", ", ChatColor.GRAY, null));
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (Messaging.isPlayer(sender)) {
                    Player senderPlayer = (Player) sender;
                    String pointName = args[1];
                    BattlePoint newPoint = new BattlePoint(senderPlayer.getLocation(), pointName);
                    om.addBattlePoint(newPoint);
                    Messaging.send(senderPlayer, "bp.added", pointName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeBattlePoint(args[1])) {
                    Messaging.send(sender, "bp.removed", args[1]);
                } else {
                    Messaging.send(sender, "bp.no-such", args[1]);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                BattlePoint battlePoint = om.getBattlePoint(args[1]);
                if (battlePoint != null) {
                    Messaging.send(sender, "bp.info", battlePoint.getName(), LocUtils.toStringWorldXYZ(battlePoint.getLoc()));
                } else {
                    Messaging.send(sender, "bp.no-such", args[1]);
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("tp")) {
                Player player = Messaging.getPlayer(args[1], sender);
                if (player != null) {
                    String bPointName = args[2];
                    BattlePoint bPoint = om.getBattlePoint(bPointName);
                    if (bPoint != null) {
                        PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                        pvpPlayer.tpToBattlePoint(bPoint);
                    } else {
                        Messaging.send(sender, "bp.no-such", bPointName);
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("tpprefix")) {
                Player player = Messaging.getPlayer(args[1], sender);
                if (player != null) {
                    String bPointPrefix = args[2];
                    BattlePoint bPoint = om.getRandomBattlePoint(bPointPrefix);
                    if (bPoint != null) {
                        PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                        pvpPlayer.tpToBattlePoint(bPoint);
                    } else {
                        Messaging.send(sender, "bp.no-match", bPointPrefix);
                    }
                }
                return true;
            }
        }

        return false;
    }
}
