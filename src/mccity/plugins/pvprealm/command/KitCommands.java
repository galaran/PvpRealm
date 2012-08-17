package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.object.ItemsKit;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommands implements SubCommandExecutor {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                StringBuilder kitList = new StringBuilder();
                for (ItemsKit curKit : om.getKits()) {
                    kitList.append(curKit.getName());
                    kitList.append(' ');
                }
                GUtils.sendMessage(sender, "Kits: " + kitList.toString());
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (sender instanceof Player) {
                    Player senderPlayer = (Player) sender;
                    ItemStack[] invContent = senderPlayer.getInventory().getContents();
                    ItemsKit newKit = new ItemsKit(args[1], invContent);
                    om.addKit(newKit);
                    GUtils.sendMessage(sender, "Your inventory content saved as kit '" + newKit.getName() + "'");
                } else {
                    GUtils.sendMessage(sender, "This command can be executed only by a player");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeKit(args[1])) {
                    GUtils.sendMessage(sender, "Kit " + args[1] + " has been removed");
                } else {
                    GUtils.sendMessage(sender, "No such kit: " + args[1]);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                ItemsKit kit = om.getKit(args[1]);
                if (kit != null) {
                    ItemStack[] stacks = kit.getStacks();
                    GUtils.sendMessage(sender, "Kit '" + kit.getName() + "' content (" + stacks.length + "):");
                    for (ItemStack curStack : stacks) {
                        GUtils.sendMessage(sender, ChatColor.RED + "- " + ChatColor.WHITE + GUtils.stackToString(curStack));
                    }
                } else {
                    GUtils.sendMessage(sender, "No such kit: " + args[1]);
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                String playerName = args[1];
                String kitName = args[2];

                Player player = Bukkit.getServer().getPlayerExact(playerName);
                if (player != null) {
                    ItemsKit kit = om.getKit(kitName);
                    if (kit != null) {
                        PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                        pvpPlayer.giveKit(kit, true);
                    } else {
                        GUtils.sendMessage(sender, "No such kit: " + kitName);
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
