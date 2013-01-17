package mccity.plugins.pvprealm.listeners;

import com.google.common.collect.Maps;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import me.galaran.bukkitutils.pvprealm.Pair;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;

/*
 * When player respawn after death in Death-no-Drop region, it receives inventory back
 */
public class DeathNoDropListener implements Listener {

    private final Map<String, Pair<ItemStack[], ItemStack[]>> storedInventories = Maps.newHashMap();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        List<String> regions = PvpRealm.getSelf().getWorldGuard().getRegions(deathLoc);
        for (String regionId : regions) {
            if (Settings.isDndRegion(regionId, deathLoc.getWorld())) {
                event.getDrops().clear();

                PlayerInventory inv = player.getInventory();
                storedInventories.put(player.getName(),
                        new Pair<ItemStack[], ItemStack[]>(inv.getContents(), inv.getArmorContents()));
                
                Messaging.send(player, "dnd.nodrop");
                Messaging.debug("$1 dead in the no-drop region $2 [$3] and keep inventory",
                        player.getName(), regionId, deathLoc.getWorld().getName());
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Pair<ItemStack[], ItemStack[]> invContent = storedInventories.remove(player.getName());
        if (invContent != null) {
            PlayerInventory storedInv = player.getInventory();
            storedInv.setContents(invContent.getLeft());
            storedInv.setArmorContents(invContent.getRight());
            player.updateInventory();
        }
    }
}
