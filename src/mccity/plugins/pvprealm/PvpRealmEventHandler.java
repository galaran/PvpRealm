package mccity.plugins.pvprealm;

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PvpRealmEventHandler implements Listener {

    private final PvpRealm pvpRealm;
    private static boolean checkTeleport = true;

    public PvpRealmEventHandler(PvpRealm pvpRealm) {
        this.pvpRealm = pvpRealm;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHeroExperienceChange(ExperienceChangeEvent event) {
        if (Config.deathExpLoss && event.getSource() == HeroClass.ExperienceType.DEATH &&
                event.getLocation().getWorld().equals(Config.getPvpWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!checkTeleport) return;

        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();
        if (!from.equals(to)) {
            PvpPlayer pvpPlayer = ObjectManager.instance.getPvpPlayer(event.getPlayer());
            if (from.equals(Config.getPvpWorld())) {
                pvpPlayer.onSideTeleportOut();
            } else if (to.equals(Config.getPvpWorld())) {
                pvpPlayer.onSideTeleportIn(event.getFrom());
            }
        }
    }

    public static void setCheckTeleport(boolean checkTelep) {
        checkTeleport = checkTelep;
    }
}
