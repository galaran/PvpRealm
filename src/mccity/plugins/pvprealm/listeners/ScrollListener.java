package mccity.plugins.pvprealm.listeners;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
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

    private final Map<Player, Integer> playersUsingScroll = new HashMap<Player, Integer>();

    public ScrollListener(PvpRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Config.scroll) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack handStack = event.getItem();
        if (handStack == null || !handStack.getData().equals(Config.scrollItem)) return;

        Player player = event.getPlayer();
        if (!player.hasPermission(PERM_SCROLL)) {
            GUtils.sendTranslated(player, "scroll.no-perm");
            return;
        }

        if (player.getLocation().getWorld().equals(Config.pvpWorld)) {
            GUtils.sendTranslated(player, "scroll.already-in-pvp-world");
            return;
        }

        if (playersUsingScroll.containsKey(player)) {
            GUtils.sendTranslated(player, "scroll.already-use");
            return;
        }

        playersUsingScroll.put(player, Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                new ScrollTask(player), Config.scrollDelaySec * 20));
        GUtils.sendTranslated(player, "scroll.using", Config.scrollDelaySec);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Config.scroll) return;

        Player player = event.getPlayer();
        Integer taskId = playersUsingScroll.remove(player);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
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
                if (handStack != null && handStack.getData().equals(Config.scrollItem)) {
                    if (Config.consumeScroll) {
                        if (handStack.getAmount() > 1) {
                            handStack.setAmount(handStack.getAmount() - 1);
                        } else {
                            player.setItemInHand(null);
                        }
                    }
                    ObjectManager.instance.getPvpPlayer(player).enterPvpRealm();
                } else {
                    GUtils.sendTranslated(player, "scroll.not-in-hand");
                }
            }
            playersUsingScroll.remove(player);
        }
    }
}
