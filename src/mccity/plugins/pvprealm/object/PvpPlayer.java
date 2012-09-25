package mccity.plugins.pvprealm.object;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.party.HeroParty;
import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.listeners.PvpRealmListener;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class PvpPlayer implements ConfigurationSerializable {

    private final PvpRealm plugin;

    private final String name;
    private Player player;
    private Location returnLoc;

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

    @SuppressWarnings("deprecation")
    public void giveKit(ItemsKit kit, boolean dropIfFull) {
        Inventory inv = player.getInventory();
        HashMap<Integer,ItemStack> ungiven = inv.addItem(kit.getStacks());
        if (ungiven != null && !ungiven.isEmpty()) {
            GUtils.sendMessage(player, "You have not enought slots in the inventory");
            if (dropIfFull) {
                World world = player.getWorld();
                for (ItemStack ungivenStack : ungiven.values()) {
                    world.dropItem(player.getLocation().add(0, 2, 0), ungivenStack);
                }
            }
        }
        player.updateInventory();
        GUtils.sendMessage(player, "You have obtain kit " + ChatColor.GOLD + kit.getName());
    }

    public void enterPvpRealm() {
        Location curLoc = player.getLocation();
        if (curLoc.getWorld().equals(Config.pvpWorld)) { // no action required
            GUtils.log("Player " + player.getName() + " entering to pvp world " + Config.pvpWorld.getName() +
                    " but it already in", Level.WARNING);
            return;
        }

        if (teleportUnchecked(Config.entryLoc)) {
            returnLoc = curLoc;
        } else {
            GUtils.log("Failed to teleport player " + player.getName() + " into pvp world ", Level.WARNING);
        }
    }

    public void leavePvpRealm() {
        Location curLoc = player.getLocation();
        String playerName = player.getName();
        if (!curLoc.getWorld().equals(Config.pvpWorld)) { // no action required
            GUtils.log("Player " + playerName + " leaving pvp world " + Config.pvpWorld.getName() +
                    " but already out of it at loc " + GUtils.locToStringWorldXYZ(curLoc), Level.WARNING);
            return;
        }

        Location returnLoc = this.returnLoc;
        if (returnLoc == null) {
            GUtils.log("Return loc not found for player " + playerName, Level.WARNING);
            returnLoc = Config.defaultReturnLoc;
        }

        if (teleportUnchecked(returnLoc)) {
            this.returnLoc = null;
        } else {
            GUtils.log("Failed to teleport player " + playerName + " out of the pvp world ", Level.WARNING);
        }
    }

    public boolean tpToBattlePoint(String prefix) {
        List<BattlePoint> points = ObjectManager.instance.getBattlePoints();
        List<BattlePoint> matchedPoints = new ArrayList<BattlePoint>();
        for (BattlePoint curPoint : points) {
            if (curPoint.getName().startsWith(prefix)) {
                matchedPoints.add(curPoint);
            }
        }
        if (matchedPoints.isEmpty()) {
            return false;
        }

        BattlePoint targetPoint = matchedPoints.get(GUtils.random.nextInt(matchedPoints.size()));
        if (!teleportUnchecked(targetPoint.getLoc())) {
            GUtils.log("Failed to teleport player " + player.getName() + " to tp point " + targetPoint.getName(), Level.WARNING);
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

    public void onSideTeleportOut() {
        returnLoc = null;
        GUtils.sendMessage(player, "You has been side-teleported out of the Pvp World");
    }

    public void onSideTeleportIn(Location from) {
        returnLoc = from;
        GUtils.sendMessage(player, "You has been side-teleported to the Pvp World");
    }

    public String getName() {
        return name;
    }

    public void load(ConfigurationSection section) {
        ConfigurationSection returnSection = section.getConfigurationSection("return-loc");
        if (returnSection != null) {
            returnLoc = GUtils.deserializeLocation(returnSection.getValues(false));
        } else {
            returnLoc = null;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (returnLoc != null) {
            result.put("return-loc", GUtils.serializeLocation(returnLoc));
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
}
