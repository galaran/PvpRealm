package me.galaran.bukkitutils.pvprealm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DoOrNotify {

    private static String playerNotifyPattern = ChatColor.RED + "No player with name $1";
    private static String worldNotifyPattern = ChatColor.RED + "World $1 not loaded";
    private static String csNotPlayerPattern = ChatColor.RED + "This command can be executed only by a player";

    static void setNotifyMessages(String playerMess, String worldMess, String csNotPlayerMess) {
        if (playerMess != null) {
            playerNotifyPattern = playerMess;
        }
        if (worldMess != null) {
            worldNotifyPattern = worldMess;
        }
        if (csNotPlayerMess != null) {
            csNotPlayerPattern = csNotPlayerMess;
        }
    }

    public static Player getPlayer(String name, boolean exact, CommandSender notifyTo) {
        Player player = exact ? Bukkit.getPlayerExact(name) : Bukkit.getPlayer(name);
        if (player == null) {
            GUtils.sendMessage(notifyTo, playerNotifyPattern, exact ? name : "starting with " + name);
        }
        return player;
    }

    public static World getWorld(String name, CommandSender notifyTo) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            GUtils.sendMessage(notifyTo, worldNotifyPattern, name);
        }
        return world;
    }

    public static boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            GUtils.sendMessage(sender, csNotPlayerPattern);
            return false;
        }
        return true;
    }
}
