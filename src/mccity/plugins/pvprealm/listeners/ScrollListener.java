package mccity.plugins.pvprealm.listeners;

import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.Pair;
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
        if (handStack == null || !handStack.getData().equals(Settings.scrollItem)) return;

        Player player = event.getPlayer();
        if (!player.hasPermission(PERM_SCROLL)) {
            GUtils.sendTranslated(player, "scroll.no-perm");
            return;
        }

        if (player.getLocation().getWorld().equals(Settings.pvpWorld)) {
            GUtils.sendTranslated(player, "scroll.already-in-pvp-world");
            return;
        }

        if (playersUsingScroll.containsKey(player)) {
            GUtils.sendTranslated(player, "scroll.already-use");
            return;
        }

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ScrollTask(player), Settings.scrollDelaySec * 20);
        playersUsingScroll.put(player, new Pair<Integer, Location>(taskId, player.getLocation()));
        GUtils.sendTranslated(player, "scroll.using", Settings.scrollDelaySec);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Settings.scroll) return;

        Player player = event.getPlayer();
        Pair<Integer, Location> taskLoc = playersUsingScroll.get(player);
        if (taskLoc == null) return;

        if (!GUtils.isPositionsEquals(player.getLocation(), taskLoc.getRight(), 0.1)) {
            playersUsingScroll.remove(player);
            Bukkit.getScheduler().cancelTask(taskLoc.getLeft());
            GUtils.sendTranslated(player, "scroll.aborted");
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
                if (handStack != null && handStack.getData().equals(Settings.scrollItem)) {
                    if (Settings.consumeScroll) {
                        if (handStack.getAmount() > 1) {
                            handStack.setAmount(handStack.getAmount() - 1);
                        } else {
                            player.setItemInHand(null);
                        }
                    }
                    ObjectManager.instance.getPvpPlayer(player).enterPvpRealm();
                    if (Settings.scrollBroadcastArrival) {
                        GUtils.serverBroadcast(GUtils.getProcessedTranslation("scroll.arrival-message", player.getName()));
                    }
                } else {
                    GUtils.sendTranslated(player, "scroll.not-in-hand");
                }
            }
            playersUsingScroll.remove(player);
        }
    }
}
