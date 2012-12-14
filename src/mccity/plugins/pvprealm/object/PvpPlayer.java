package mccity.plugins.pvprealm.object;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.party.HeroParty;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.listeners.PvpRealmListener;
import me.galaran.bukkitutils.pvprealm.LocUtils;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class PvpPlayer implements ConfigurationSerializable {

    private final PvpRealm plugin;

    private final String name;
    private Player player;

    private volatile Location returnLoc;

    private static final String PERM_BYPASS_RETURN_RMEFFECTS = "pvprealm.bypass.returnrmeffects";

    public PvpPlayer(PvpRealm plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        name = player.getName();
        returnLoc = null;
    }

    public void updateEntity(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public void enterPvpRealm() {
        Location curLoc = player.getLocation();
        if (curLoc.getWorld().equals(Settings.pvpWorld)) { // no action required
            Messaging.log(Level.WARNING, "Player " + player.getName() + " entering to pvp world " + Settings.pvpWorld.getName() +
                    " but it already in");
            return;
        }

        if (teleportUnchecked(Settings.pvpwEntryLoc)) {
            returnLoc = curLoc;
        } else {
            Messaging.log(Level.WARNING, "Failed to teleport player " + player.getName() + " into pvp world ");
        }
    }

    public void leavePvpRealm() {
        Location curLoc = player.getLocation();
        String playerName = player.getName();
        if (!curLoc.getWorld().equals(Settings.pvpWorld)) { // no action required
            Messaging.log(Level.WARNING, "Player " + playerName + " leaving pvp world " + Settings.pvpWorld.getName() +
                    " but already out of it at loc " + LocUtils.toStringWorldXYZ(curLoc));
            return;
        }

        Location returnLoc = this.returnLoc;
        if (returnLoc == null) {
            Messaging.log(Level.WARNING, "No return loc for player " + playerName);
            returnLoc = Settings.pvpwDefaultReturnLoc;
        }

        if (teleportUnchecked(returnLoc)) {
            this.returnLoc = null;

            if (!player.hasPermission(PERM_BYPASS_RETURN_RMEFFECTS)) {
                clearEffects();
            }
        } else {
            Messaging.log(Level.SEVERE, "Failed to teleport player " + playerName + " out of the pvp world ");
        }
    }

    public boolean tpToBattlePoint(BattlePoint battlePoint) {
        if (!teleportUnchecked(battlePoint.getLoc())) {
            Messaging.log(Level.SEVERE, "Failed to teleport player " + player.getName() + " to tp point " + battlePoint.getName());
        }
        return true;
    }

    private boolean teleportUnchecked(Location loc) {
        PvpRealmListener.setCheckTeleport(false);
        try {
            return player.teleport(loc);
        } finally {
            PvpRealmListener.setCheckTeleport(true);
        }
    }

    public void onSideTeleportIn(Location from) {
        returnLoc = from;
        Messaging.send(player, "world.side-teleport-in");
    }

    public void onSideTeleportOut() {
        returnLoc = null;
        Messaging.send(player, "world.side-teleport-out");
    }

    public void load(ConfigurationSection section) {
        ConfigurationSection returnSection = section.getConfigurationSection("return-loc");
        if (returnSection != null) {
            returnLoc = LocUtils.deserialize(returnSection);
        } else {
            returnLoc = null;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (returnLoc != null) {
            result.put("return-loc", LocUtils.serialize(returnLoc));
        }
        return result;
    }

    public boolean hasFriend(Player player) {
        Hero hero = plugin.getHero(this);
        HeroParty party = hero.getParty();
        if (party != null && party.isPartyMember(player)) return true;

        if (plugin.isUsingTowny()) {
            if (plugin.getTowny().isTownyFriendly(this.player, player)) return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PvpPlayer pvpPlayer = (PvpPlayer) o;

        if (name != null ? !name.equals(pvpPlayer.name) : pvpPlayer.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public void clearEffects() {
        boolean cleared = false;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
            cleared = true;
        }
        if (cleared) {
            Messaging.send(player, "signs.rmeffects.cleared-message");
        }
    }

    public Hero getHero() {
        return plugin.getHero(this);
    }

    public void restore() {
        Hero hero = getHero();
        hero.setHealth(hero.getMaxHealth());
        hero.syncHealth();
        hero.setMana(hero.getMaxMana());
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
    }
}
