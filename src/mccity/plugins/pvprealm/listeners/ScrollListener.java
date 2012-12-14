package mccity.plugins.pvprealm.listeners;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.LocUtils;
import me.galaran.bukkitutils.pvprealm.Pair;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ScrollListener implements Listener {

    private static final String PERM_SCROLL = "pvprealm.enterscroll";

    private final PvpRealm plugin;

    // Player -> Task id, Initial location
    private final Map<Player, Pair<Integer, Location>> playersUsingScroll = new HashMap<Player, Pair<Integer, Location>>();

    public ScrollListener(PvpRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Settings.scroll) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack handStack = event.getItem();
        if (!Settings.scrollItem.matches(handStack)) return;

        Player player = event.getPlayer();
        if (!player.hasPermission(PERM_SCROLL)) {
            Messaging.send(player, "scroll.no-perm");
            return;
        }

        if (player.getLocation().getWorld().equals(Settings.pvpWorld)) {
            Messaging.send(player, "scroll.already-in-pvp-world");
            return;
        }

        if (playersUsingScroll.containsKey(player)) {
            Messaging.send(player, "scroll.already-use");
            return;
        }

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ScrollTask(player), Settings.scrollDelaySec * 20);
        playersUsingScroll.put(player, new Pair<Integer, Location>(taskId, player.getLocation()));
        Messaging.send(player, "scroll.using", Settings.scrollDelaySec);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Settings.scroll) return;

        Player player = event.getPlayer();
        Pair<Integer, Location> taskLoc = playersUsingScroll.get(player);
        if (taskLoc == null) return;

        if (!LocUtils.closerThan(player.getLocation(), taskLoc.getRight(), 0.1)) {
            playersUsingScroll.remove(player);
            Bukkit.getScheduler().cancelTask(taskLoc.getLeft());
            Messaging.send(player, "scroll.aborted");
        }
    }

    private class ScrollTask implements Runnable {

        private final Player player;

        public ScrollTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (player.isOnline()) {
                ItemStack handStack = player.getItemInHand();
                if (Settings.scrollItem.matches(handStack)) {
                    if (Settings.consumeScroll) {
                        if (handStack.getAmount() > 1) {
                            handStack.setAmount(handStack.getAmount() - 1);
                        } else {
                            player.setItemInHand(null);
                        }
                    }
                    ObjectManager.instance.getPvpPlayer(player).enterPvpRealm();
                    if (Settings.scrollBroadcastArrival) {
                        Messaging.broadcastServerNoPrefix("scroll.arrival-message", player.getName());
                    }
                } else {
                    Messaging.send(player, "scroll.not-in-hand");
                }
            }
            playersUsingScroll.remove(player);
        }
    }
}
