package mccity.plugins.pvprealm.listeners;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import me.galaran.bukkitutils.pvprealm.GUtils;
import net.minecraft.server.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on AdminCmd NoDrop code
 * https://github.com/Belphemur/AdminCmd/blob/master/src/main/java/be/Balor/Listeners/Features/ACNoDropListener.java
 *
 * Requires WorldGuard to check death region
 */
 public class DeathNoDropListener implements Listener {

    private final PvpRealm plugin;
    private final Map<Player, PlayerInv> itemsDrops = new HashMap<Player, PlayerInv>();

    public DeathNoDropListener(PvpRealm pvpRealm) {
        plugin = pvpRealm;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        List<String> regions = plugin.getWorldGuard().getRegions(deathLoc);
        for (String regionId : regions) {
            if (Config.isDndRegion(regionId, deathLoc.getWorld())) {
                event.getDrops().clear();
                itemsDrops.put(player, new PlayerInv(player));
                GUtils.sendTranslated(player, "dnd.nodrop");
                if (Config.debug) {
                    GUtils.log(player.getName() + " dead in the dnd region " + regionId +
                            "[" + deathLoc.getWorld().getName() + "] and keep inventory");
                }
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerInv inv = itemsDrops.remove(player);
        if (inv != null) {
            inv.setInventory(player);
            player.updateInventory();
        }
    }

    private static class PlayerInv {

        private final net.minecraft.server.ItemStack items[];
        private final net.minecraft.server.ItemStack armor[];

        public PlayerInv(Player p) {
            EntityPlayer player = ((CraftPlayer) p).getHandle();
            items = Arrays.copyOf(player.inventory.items,
                    player.inventory.items.length);
            armor = Arrays.copyOf(player.inventory.armor,
                    player.inventory.armor.length);
        }

        public void setInventory(Player p) {
            EntityPlayer player = ((CraftPlayer) p).getHandle();
            player.inventory.armor = this.armor;
            player.inventory.items = this.items;
        }
    }
}
