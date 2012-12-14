package mccity.plugins.pvprealm.listeners;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import me.galaran.bukkitutils.pvprealm.nms.InventoryPlayer;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

 public class DeathNoDropListener implements Listener {

    private final PvpRealm plugin;
    private final Map<Player, InventoryPlayer> itemsDrops = new HashMap<Player, InventoryPlayer>();

    public DeathNoDropListener(PvpRealm pvpRealm) {
        plugin = pvpRealm;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLoc = player.getLocation();
        List<String> regions = plugin.getWorldGuard().getRegions(deathLoc);
        for (String regionId : regions) {
            if (Settings.isDndRegion(regionId, deathLoc.getWorld())) {
                event.getDrops().clear();
                itemsDrops.put(player, new InventoryPlayer(player));
                Messaging.send(player, "dnd.nodrop");
                if (Settings.debug) {
                    Messaging.log("$1 dead in the no-drop region $2 [$3] and keep inventory",
                            player.getName(), regionId, deathLoc.getWorld().getName());
                }
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        InventoryPlayer inv = itemsDrops.remove(player);
        if (inv != null) {
            inv.setInventory(player);
            player.updateInventory();
        }
    }
}
