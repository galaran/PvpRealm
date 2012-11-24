package me.galaran.bukkitutils.pvprealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DoOrNotify {

    public static Player getPlayer(String name, boolean exact, CommandSender sender) {
        Player player = exact ? Bukkit.getPlayerExact(name) : Bukkit.getPlayer(name);
        if (player == null) {
            GUtils.sendTranslated(sender, exact ? "utils.no-player" : "utils.no-player-matching", name);
        }
        return player;
    }

    public static World getWorld(String name, CommandSender sender) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            GUtils.sendTranslated(sender, "utils.no-world", name);
        }
        return world;
    }

    public static boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            GUtils.sendTranslated(sender, "utils.cs-not-player");
            return false;
        }
        return true;
    }

    public static boolean hasPermissionWithNotify(Player player, String perm, CommandSender sender) {
        boolean has = player.hasPermission(perm);
        if (!has) {
            GUtils.sendTranslated(sender, "utils.no-perm");
        }
        return has;
    }
}
