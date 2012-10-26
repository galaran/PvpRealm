package mccity.plugins.pvprealm.command;

import mccity.plugins.pvprealm.object.LootSet;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.DoOrNotify;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class LootSetCommands implements SubCommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        ObjectManager om = ObjectManager.instance;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                GUtils.sendTranslated(sender, "lootset.list",
                        StringUtils.join(om.listLootSets(), ChatColor.YELLOW, ", ", ChatColor.GRAY, null));
                return true;
            }
        } else if (args.length == 2) {
            String lootSetName = args[1];
            if (args[0].equalsIgnoreCase("new")) {
                boolean replaced = om.addLootSet(lootSetName);
                if (replaced) {
                    GUtils.sendTranslated(sender, "lootset.replaced", lootSetName);
                } else {
                    GUtils.sendTranslated(sender, "lootset.added", lootSetName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (om.removeLootSet(lootSetName)) {
                    GUtils.sendTranslated(sender, "lootset.removed", lootSetName);
                } else {
                    GUtils.sendTranslated(sender, "lootset.no-such", lootSetName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                LootSet lootSet = getLootSetOrNotify(lootSetName, sender);
                if (lootSet != null) {
                    Map<String, Float> lootSetMapping = lootSet.getKitMapping();
                    GUtils.sendTranslated(sender, "lootset.content-header", lootSet.getName(), lootSetMapping.size());
                    for (Map.Entry<String, Float> kitEntry : lootSetMapping.entrySet()) {
                        String kitName = kitEntry.getKey();
                        String coloredKitName = (om.getKit(kitName) == null ? ChatColor.DARK_RED : ChatColor.GREEN) + kitName;
                        sender.sendMessage(StringUtils.decorateString("$1&f - &b$2%", coloredKitName, StringUtils.formatDouble(kitEntry.getValue() * 100)));
                    }
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("rmkit")) {
                LootSet lootSet = getLootSetOrNotify(args[1], sender);
                if (lootSet != null) {
                    String kitName = args[2];
                    if (lootSet.getKitMapping().remove(kitName) != null) {
                        GUtils.sendTranslated(sender, "lootset.kit.removed", kitName, lootSet.getName());
                    } else {
                        GUtils.sendTranslated(sender, "lootset.kit.no-such", kitName, lootSet.getName());
                    }
                }
                return true;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("addkit")) {
                LootSet lootSet = getLootSetOrNotify(args[1], sender);
                if (lootSet != null) {
                    String kitName = args[2];
                    float chancePercent;
                    try {
                        chancePercent = Float.parseFloat(args[3]);
                    } catch (NumberFormatException ex) {
                        return false;
                    }

                    boolean added = lootSet.getKitMapping().put(kitName, chancePercent / 100f) == null;
                    String chancePercentFormatted = StringUtils.formatDouble(chancePercent);
                    if (added) {
                        GUtils.sendTranslated(sender, "lootset.kit.added", kitName, lootSet.getName(), chancePercentFormatted);
                    } else {
                        GUtils.sendTranslated(sender, "lootset.kit.replaced", kitName, lootSet.getName(), chancePercentFormatted);
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("give")) {
                Player player = DoOrNotify.getPlayer(args[1], true, sender);
                if (player != null) {
                    LootSet lootSet = getLootSetOrNotify(args[2], sender);
                    if (lootSet != null) {
                        PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                        pvpPlayer.giveKit(lootSet.rollKit(), true, Boolean.parseBoolean(args[3]));
                    }
                }
                return true;
            }
        }

        return false;
    }

    public LootSet getLootSetOrNotify(String lootSetName, CommandSender sender) {
        LootSet result = ObjectManager.instance.getLootSet(lootSetName);
        if (result == null) {
            GUtils.sendTranslated(sender, "lootset.no-such", lootSetName);
        }
        return result;
    }
}
