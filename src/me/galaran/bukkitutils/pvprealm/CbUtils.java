package me.galaran.bukkitutils.pvprealm;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CbUtils {

    /** Bukkit's World.dropItem() drops stack with null NBT tag, this method not */
    public static void dropStackSafe(ItemStack stack, Location loc) {
        net.minecraft.server.World nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        net.minecraft.server.ItemStack nmsStack = ((CraftItemStack) stack.clone()).getHandle();

        net.minecraft.server.EntityItem entity =
                new net.minecraft.server.EntityItem(nmsWorld, loc.getX(), loc.getY(), loc.getZ(), nmsStack);
        entity.pickupDelay = 10;
        nmsWorld.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    /**
     * Gives deep clone of stacks, and drop ungiven, if not fit to inventory
     * @return is there was no enought inventory space and some stacks dropped
     */
    @SuppressWarnings("deprecation")
    public static boolean giveStacksOrDrop(Player player, ItemStack... stacks) {
        ItemStack[] cloneStacks = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            cloneStacks[i] = stacks[i].clone();
        }

        Inventory inv = player.getInventory();
        HashMap<Integer, ItemStack> ungiven = inv.addItem(cloneStacks);
        boolean dropped = false;
        if (ungiven != null && !ungiven.isEmpty()) {
            dropped = true;
            for (ItemStack ungivenStack : ungiven.values()) {
                dropStackSafe(ungivenStack, player.getEyeLocation());
            }
        }
        player.updateInventory();

        return dropped;
    }
}
