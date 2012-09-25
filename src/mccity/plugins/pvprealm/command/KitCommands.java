package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.object.ItemsKit;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.DoOrNotify;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.Lang;
import me.galaran.bukkitutils.pvprealm.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommands implements SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GUtils.sendTranslated(sender, "kit.list",
                        StringUtils.join(om.getKits(), ChatColor.GOLD, ", ", ChatColor.GRAY, null));
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (DoOrNotify.isPlayer(sender)) {
                    Player senderPlayer = (Player) sender;
                    ItemStack[] invContent = senderPlayer.getInventory().getContents();
                    ItemsKit newKit = new ItemsKit(args[1], invContent);
                    om.addKit(newKit);
                    GUtils.sendTranslated(sender, "kit.saved-as", newKit.getName());
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeKit(args[1])) {
                    GUtils.sendTranslated(sender, "kit.removed", args[1]);
                } else {
                    GUtils.sendTranslated(sender, "kit.no-such", args[1]);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                ItemsKit kit = om.getKit(args[1]);
                if (kit != null) {
                    ItemStack[] stacks = kit.getStacks();
                    GUtils.sendTranslated(sender, "kit.content-header", kit.getName(), stacks.length);
                    for (ItemStack curStack : stacks) {
                        GUtils.sendMessage(sender, ChatColor.RED + "- " + ChatColor.WHITE + GUtils.stackToString(curStack));
                    }
                } else {
                    GUtils.sendTranslated(sender, "kit.no-such", args[1]);
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                Player player = DoOrNotify.getPlayer(args[1], true, sender);
                if (player != null) {
                    String kitName = args[2];
                    ItemsKit kit = om.getKit(kitName);
                    if (kit != null) {
                        PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                        pvpPlayer.giveKit(kit, true);
                    } else {
                        GUtils.sendTranslated(sender, "kit.no-such", kitName);
                    }
                }
                return true;
            }
        }

        return false;
    }
}
