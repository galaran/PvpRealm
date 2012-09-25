package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.DoOrNotify;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.Lang;
import me.galaran.bukkitutils.pvprealm.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BattlePointCommands implements SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GUtils.sendTranslated(sender, "bp.list",
                        StringUtils.join(om.getBattlePoints(), ChatColor.DARK_PURPLE, ", ", ChatColor.GRAY, null));
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (DoOrNotify.isPlayer(sender)) {
                    Player senderPlayer = (Player) sender;
                    String pointName = args[1];
                    BattlePoint newPoint = new BattlePoint(senderPlayer.getLocation(), pointName);
                    om.addBattlePoint(newPoint);
                    GUtils.sendTranslated(senderPlayer, "bp.added", pointName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeBattlePoint(args[1])) {
                    GUtils.sendTranslated(sender, "bp.removed", args[1]);
                } else {
                    GUtils.sendTranslated(sender, "bp.no-such", args[1]);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                BattlePoint battlePoint = om.getBattlePoint(args[1]);
                if (battlePoint != null) {
                    GUtils.sendTranslated(sender, "bp.info", battlePoint.getName(), GUtils.locToStringWorldXYZ(battlePoint.getLoc()));
                } else {
                    GUtils.sendTranslated(sender, "bp.no-such", args[1]);
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("tp")) {
                Player player = DoOrNotify.getPlayer(args[1], true, sender);
                if (player != null) {
                    String bpPrefix = args[2];
                    PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                    if (!pvpPlayer.tpToBattlePoint(bpPrefix)) {
                        GUtils.sendTranslated(sender, "bp.no-match", bpPrefix);
                    }
                }
                return true;
            }
        }

        return false;
    }
}
