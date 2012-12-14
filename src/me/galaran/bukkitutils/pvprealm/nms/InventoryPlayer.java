package me.galaran.bukkitutils.pvprealm.nms;

import net.minecraft.server.EntityPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Based on AdminCmd NoDrop code
 * https://github.com/Belphemur/AdminCmd/blob/master/src/main/java/be/Balor/Listeners/Features/ACNoDropListener.java
 */
public class InventoryPlayer {

    private final net.minecraft.server.ItemStack items[];
    private final net.minecraft.server.ItemStack armor[];

    public InventoryPlayer(Player p) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        items = Arrays.copyOf(player.inventory.items,
                player.inventory.items.length);
        armor = Arrays.copyOf(player.inventory.armor,
                player.inventory.armor.length);
    }

    public void setInventory(Player p) {
        EntityPlayer player = ((CraftPlayer) p).getHandle();
        player.inventory.armor = armor;
        player.inventory.items = items;
    }
}
