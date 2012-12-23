package mccity.plugins.pvprealm.object;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.persistence.YmlStorage;
import me.galaran.bukkitutils.pvprealm.RandomUtils;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectManager {

    private static final int AUTOSAVE_PERIOD = 300 * 20;

    public static ObjectManager instance;
    private final PvpRealm plugin;

    private final YmlStorage storage;

    private final Map<String, PvpPlayer> pvpPlayers = new ConcurrentHashMap<String, PvpPlayer>();
    private final Set<String> unloadPlayers = Collections.synchronizedSet(new HashSet<String>());

    private final Map<String, BattlePoint> battlePoints = new LinkedHashMap<String, BattlePoint>();

    public static void init(PvpRealm plugin) {
        instance = new ObjectManager(plugin);
    }

    private ObjectManager(PvpRealm plugin) {
        this.plugin = plugin;
        storage = new YmlStorage(plugin);

        List<BattlePoint> loadedPoints = storage.loadBattlePoints();
        for (BattlePoint battlePoint : loadedPoints) {
            battlePoints.put(battlePoint.getName(), battlePoint);
        }
        Messaging.log(loadedPoints.size() + " battle points");

        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), plugin);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new AutoSaveTask(), AUTOSAVE_PERIOD, AUTOSAVE_PERIOD);
    }

    public PvpPlayer getPvpPlayer(Player player) {
        PvpPlayer result = pvpPlayers.get(player.getName());
        if (result == null) {
            result = storage.loadPvpPlayer(player);
            if (result == null) {
                result = new PvpPlayer(plugin, player);
                if (Settings.debug) {
                    Messaging.log("New Pvp player: $1", result.getName());
                }
            }
            pvpPlayers.put(result.getName(), result);
        }
        return result;
    }

    public List<BattlePoint> listBattlePoints() {
        return new ArrayList<BattlePoint>(battlePoints.values());
    }

    public BattlePoint getBattlePoint(String bpName) {
        return battlePoints.get(bpName);
    }

    public BattlePoint getRandomBattlePoint(String bpPrefix) {
        List<BattlePoint> matchedBps = new ArrayList<BattlePoint>();
        for (BattlePoint curBp : listBattlePoints()) {
            if (curBp.getName().startsWith(bpPrefix)) {
                matchedBps.add(curBp);
            }
        }
        if (matchedBps.isEmpty()) return null;
        return matchedBps.get(RandomUtils.rnd.nextInt(matchedBps.size()));
    }

    public boolean removeBattlePoint(String bPointName) {
        boolean isRemoved = battlePoints.remove(bPointName) != null;
        if (isRemoved) {
            storage.storeBattlePoints(listBattlePoints());
        }
        return isRemoved;
    }

    /**
     * @return replaced?
     */
    public boolean addBattlePoint(BattlePoint newPoint) {
        BattlePoint prev = battlePoints.put(newPoint.getName(), newPoint);
        storage.storeBattlePoints(listBattlePoints());
        return prev != null;
    }

    public void shutdown() {
        storage.storeBattlePoints(listBattlePoints());
        for (PvpPlayer pvpPlayer : pvpPlayers.values()) {
            storage.storePvpPlayer(pvpPlayer);
        }
    }

    private class AutoSaveTask implements Runnable {

        @Override
        public void run() {
            Iterator<Map.Entry<String, PvpPlayer>> itr = pvpPlayers.entrySet().iterator();
            while (itr.hasNext()) {
                PvpPlayer pvpPlayer = itr.next().getValue();
                storage.storePvpPlayer(pvpPlayer);
                if (unloadPlayers.remove(pvpPlayer.getName())) {
                    itr.remove();
                }
            }
        }
    }

    private class PlayerJoinQuitListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            PvpPlayer pvpPlayer = pvpPlayers.get(event.getPlayer().getName());
            if (pvpPlayer != null) {
                unloadPlayers.add(pvpPlayer.getName());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            String playerName = event.getPlayer().getName();

            unloadPlayers.remove(playerName);
            PvpPlayer joined = pvpPlayers.get(playerName);
            if (joined != null) {
                joined.updateEntity(event.getPlayer());
                if (Settings.debug) {
                    Messaging.log("Updated entity (id $1) for Pvp player $2", event.getPlayer().getEntityId(), joined.getName());
                }
            }
        }
    }
}
