package mccity.plugins.pvprealm.listeners;

import com.herocraftonline.heroes.api.events.HeroLeaveCombatEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.CombatEffect;
import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.Lang;
import me.galaran.bukkitutils.pvprealm.StringUtils;
import me.galaran.bukkitutils.pvprealm.ToStringer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PvpLoggerListener implements Listener {

    private final PvpRealm plugin;

    public PvpLoggerListener(PvpRealm pvpRealm) {
        plugin = pvpRealm;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHeroLeaveCombat(HeroLeaveCombatEvent event) {
        if (event.getReason() != CombatEffect.LeaveCombatReason.LOGOUT) return;
        if (!Config.pvpLogger) return;
        Hero hero = event.getHero();
        if (!Config.pvpLoggerGlobal && !hero.getPlayer().getLocation().getWorld().equals(Config.pvpWorld)) return;

        CombatEffect combat = (CombatEffect) hero.getEffect("Combat");
        if (combat == null) return;

        Set<Player> combatPlayers = new HashSet<Player>();
        Map<LivingEntity, CombatEffect.CombatReason> combatants = combat.getCombatants();
        for (LivingEntity living : combatants.keySet()) {
            if (living instanceof Player) {
                combatPlayers.add((Player) living);
            }
        }
        combatPlayers.remove(hero.getPlayer()); // remove self

        if (!combatPlayers.isEmpty()) {
            onPvpLogout(ObjectManager.instance.getPvpPlayer(hero.getPlayer()), combatPlayers);
        }
    }

    public void onPvpLogout(PvpPlayer pvpPlayer, Set<Player> combatWith) {
        Player player = pvpPlayer.getPlayer();
        if (!Config.pvpLoggerOp && player.isOp()) return;

        if (Config.pvpLoggerBypassFriendly) {
            if (Config.debug) {
                GUtils.log(player.getName() + " logged out of pvp. Before friend check: " + combatWith.toString());
            }
            Iterator<Player> itr = combatWith.iterator();
            while (itr.hasNext()) {
                PvpPlayer curCombatPlayer = ObjectManager.instance.getPvpPlayer(itr.next());
                if (curCombatPlayer.hasFriend(player)) {
                    itr.remove();
                    if (Config.debug) {
                        GUtils.log(curCombatPlayer.getName() + " has friend " + player.getName());
                    }
                }
            }

            if (combatWith.isEmpty()) return;
        }

        String playerList = StringUtils.join(combatWith, ", ", new ToStringer<Player>() {
            @Override
            public String toString(Player obj) {
                return obj.getName();
            }
        });

        String pvpLogMessage = GUtils.getProcessedTranslation("logger.message");
        GUtils.log(pvpLogMessage);
        if (Config.pvpLoggerMessage) {
            GUtils.serverBroadcast(pvpLogMessage);
        }

        Hero hero = plugin.getHero(pvpPlayer);
        if (Config.pvpLoggerExpPenalty > 0) {
            hero.gainExp(-Config.pvpLoggerExpPenalty, HeroClass.ExperienceType.EXTERNAL, player.getLocation());
        }
        if (Config.pvpLoggerKill) {
            hero.setHealth(0);
            hero.syncHealth();
        }
    }
}
