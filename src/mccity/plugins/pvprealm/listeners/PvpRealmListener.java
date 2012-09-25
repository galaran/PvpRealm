package mccity.plugins.pvprealm.listeners;

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ItemsKit;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PvpRealmListener implements Listener {

    private final PvpRealm plugin;
    private static boolean checkTeleport = true;

    private static final String PERM_KIT_SIGN_PLACE = "pvprealm.kit.placesign";
    private static final String KIT_LINE = ChatColor.DARK_RED + "[kit]";

    public PvpRealmListener(PvpRealm pvpRealm) {
        this.plugin = pvpRealm;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHeroExperienceChange(ExperienceChangeEvent event) {
        if (Config.pvpWorldEnabled && !Config.deathHeroesExpLoss && event.getSource() == HeroClass.ExperienceType.DEATH &&
                event.getLocation().getWorld().equals(Config.pvpWorld)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!checkTeleport) return;

        if (Config.pvpWorldEnabled) {
            World from = event.getFrom().getWorld();
            World to = event.getTo().getWorld();
            if (!from.equals(to)) {
                PvpPlayer pvpPlayer = ObjectManager.instance.getPvpPlayer(event.getPlayer());
                if (from.equals(Config.pvpWorld)) {
                    pvpPlayer.onSideTeleportOut();
                } else if (to.equals(Config.pvpWorld)) {
                    pvpPlayer.onSideTeleportIn(event.getFrom());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            if (clicked.getType() == Material.SIGN || clicked.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) clicked.getState();
                handleSignClick(sign, event.getPlayer());
            }
        }
    }

    private void handleSignClick(Sign sign, Player player) {
        ObjectManager om = ObjectManager.instance;
        if (sign.getLine(1).equals(KIT_LINE)) {
            if (Config.kitSignsGlobal || (Config.pvpWorldEnabled && sign.getWorld().equals(Config.pvpWorld))) {
                String kitName = ChatColor.stripColor(sign.getLine(2).trim());
                ItemsKit kit = om.getKit(kitName);
                if (kit != null) {
                    PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                    pvpPlayer.giveKit(kit, false);
                }
            } else {
                GUtils.sendMessage(player, "Kit signs disabled out of the Pvp World", ChatColor.RED);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (ChatColor.stripColor(event.getLine(1)).trim().toLowerCase().contains("[kit]")) {
            Player player = event.getPlayer();
            if (player.hasPermission(PERM_KIT_SIGN_PLACE)) {
                event.setLine(1, KIT_LINE);
            } else {
                GUtils.sendMessage(player, ChatColor.DARK_RED + "You have no permission to place kit signs");
                event.setCancelled(true);
            }
        }
    }

    public static void setCheckTeleport(boolean checkTelep) {
        checkTeleport = checkTelep;
    }
}