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
import java.util.logging.Level;

/*
 * When player respawn after death in Death-no-Drop region, it receives inventory back
 */
public class DeathNoDropListener implements Listener {

    private final Map<String, Pair<ItemStack[], ItemStack[]>> storedInventories = Maps.newHashMap();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        List<String> dndRegions = PvpRealm.getSelf().getWorldGuard().getRegions(deathLoc);
        for (String regionId : dndRegions) {
            if (Settings.isDndRegion(regionId, deathLoc.getWorld())) {
                String name = player.getName();
                if (!storedInventories.containsKey(name)) {
                    PlayerInventory inv = player.getInventory();
                    storedInventories.put(name, new Pair<ItemStack[], ItemStack[]>(inv.getContents(), inv.getArmorContents()));

                    event.getDrops().clear();

                    Messaging.send(player, "dnd.nodrop");
                    Messaging.debug("$1 dead in the DNDR $2 [$3] and keep inventory",
                            name, regionId, deathLoc.getWorld().getName());
                } else {
                    Messaging.debug(Level.WARNING, "$1 dead in the DNDR $2 [$3]. Already has stored inventory!",
                            name, regionId, deathLoc.getWorld().getName());
                }
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
