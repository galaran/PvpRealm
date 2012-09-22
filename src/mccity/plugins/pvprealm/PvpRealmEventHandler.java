package mccity.plugins.pvprealm;

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.api.events.HeroLeaveCombatEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.effects.CombatEffect;
import mccity.plugins.pvprealm.object.ItemsKit;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PvpRealmEventHandler implements Listener {

    private final PvpRealm pvpRealm;
    private static boolean checkTeleport = true;

    private static final String PERM_KIT_SIGN_PLACE = "pvprealm.kit.placesign";
    private static final String KIT_LINE = ChatColor.DARK_RED + "[kit]";

    public PvpRealmEventHandler(PvpRealm pvpRealm) {
        this.pvpRealm = pvpRealm;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHeroExperienceChange(ExperienceChangeEvent event) {
        if (!Config.deathHeroesExpLoss && event.getSource() == HeroClass.ExperienceType.DEATH &&
                event.getLocation().getWorld().equals(Config.pvpWorld)) {
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
            if (from.equals(Config.pvpWorld)) {
                pvpPlayer.onSideTeleportOut();
            } else if (to.equals(Config.pvpWorld)) {
                pvpPlayer.onSideTeleportIn(event.getFrom());
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
            String kitName = ChatColor.stripColor(sign.getLine(2).trim());
            ItemsKit kit = om.getKit(kitName);
            if (kit != null) {
                PvpPlayer pvpPlayer = om.getPvpPlayer(player);
                pvpPlayer.giveKit(kit, false);
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
            ObjectManager.instance.getPvpPlayer(hero.getPlayer()).onPvpLogout(combatPlayers);
        }
    }
}
