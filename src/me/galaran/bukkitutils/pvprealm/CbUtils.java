package me.galaran.bukkitutils.pvprealm;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

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
}
