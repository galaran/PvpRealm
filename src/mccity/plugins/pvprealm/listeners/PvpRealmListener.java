package mccity.plugins.pvprealm.listeners;

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import mccity.plugins.pvprealm.tasks.CountdownTask;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import me.galaran.bukkitutils.pvprealm.text.StringUtils;
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
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class PvpRealmListener implements Listener {

    private final PvpRealm plugin;
    private static boolean checkTeleport = true;

    private static final String PERM_SIGN_PLACE_RMEFFECTS = "pvprealm.placesign.rmeffects";
    private static final String LINE_RMEFFECTS = ChatColor.BLUE + "[rmeffects]";

    private static final String PERM_SIGN_PLACE_COUNTDOWN = "pvprealm.placesign.countdown";
    private static final String LINE_COUNTDOWN = ChatColor.AQUA + "[countdown]";

    private static final String PERM_SIGN_PLACE_RESTORE = "pvprealm.placesign.restore";
    private static final String LINE_RESTORE = ChatColor.BLUE + "[restore]";

    public PvpRealmListener(PvpRealm pvpRealm) {
        this.plugin = pvpRealm;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (Settings.pvpwEnabled && Settings.pvpwDisableWeather && event.getWorld().equals(Settings.pvpWorld)) {
            event.setCancelled(event.toWeatherState()); // cancel if set to raining
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThunderChange(ThunderChangeEvent event) {
        if (Settings.pvpwEnabled && Settings.pvpwDisableWeather && event.getWorld().equals(Settings.pvpWorld)) {
            event.setCancelled(event.toThunderState()); // cancel if set to thundering
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHeroExperienceChange(ExperienceChangeEvent event) {
        if (Settings.pvpwEnabled && event.getLocation().getWorld().equals(Settings.pvpWorld)) {
            if (event.getSource() == HeroClass.ExperienceType.DEATH && Settings.pvpwDisableHeroesDeathExpLoss) {
                event.setCancelled(true);
            } else if (event.getSource() == HeroClass.ExperienceType.KILLING && Settings.pvpwDisableHeroesMobExp) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!checkTeleport) return;

        if (Settings.pvpwEnabled) {
            World from = event.getFrom().getWorld();
            World to = event.getTo().getWorld();
            if (!from.equals(to)) {
                PvpPlayer pvpPlayer = ObjectManager.instance.getPvpPlayer(event.getPlayer());
                if (from.equals(Settings.pvpWorld)) {
                    pvpPlayer.onSideTeleportOut();
                } else if (to.equals(Settings.pvpWorld)) {
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
                handleSignUse(sign, event.getPlayer());
            }
        }
    }

    private void handleSignUse(Sign sign, Player player) {
        ObjectManager om = ObjectManager.instance;
        if (sign.getLine(1).equals(LINE_RMEFFECTS)) {
            PvpPlayer pvpPlayer = om.getPvpPlayer(player);
            pvpPlayer.clearEffects();
        } else if (sign.getLine(1).equals(LINE_COUNTDOWN)) {
            CountdownTask cdownTask = new CountdownTask(plugin, om.getPvpPlayer(player));
            cdownTask.start();
        } else if (sign.getLine(1).equals(LINE_RESTORE)) {
            om.getPvpPlayer(player).restore();
            Messaging.broadcastNoPrefix(player.getLocation(), 30, "signs.restore.restored", player.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (StringUtils.stringContainsIgnoreCaseAndColor(event.getLine(1), LINE_RMEFFECTS)) {
            if (player.hasPermission(PERM_SIGN_PLACE_RMEFFECTS)) {
                event.setLine(1, LINE_RMEFFECTS);
            } else {
                Messaging.send(player, "signs.rmeffects.place-no-perm");
                event.setCancelled(true);
            }
        } else if (StringUtils.stringContainsIgnoreCaseAndColor(event.getLine(1), LINE_COUNTDOWN)) {
            if (player.hasPermission(PERM_SIGN_PLACE_COUNTDOWN)) {
                event.setLine(1, LINE_COUNTDOWN);
            } else {
                Messaging.send(player, "signs.countdown.place-no-perm");
                event.setCancelled(true);
            }
        } else if (StringUtils.stringContainsIgnoreCaseAndColor(event.getLine(1), LINE_RESTORE)) {
            if (player.hasPermission(PERM_SIGN_PLACE_RESTORE)) {
                event.setLine(1, LINE_RESTORE);
            } else {
                Messaging.send(player, "signs.restore.place-no-perm");
                event.setCancelled(true);
            }
        }
    }

    public static void setCheckTeleport(boolean checkTelep) {
        checkTeleport = checkTelep;
    }
}
