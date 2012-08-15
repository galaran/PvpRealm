package mccity.plugins.pvprealm.object;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.persistence.YmlStorage;
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

    private static final int AUTOSAVE_INTERVAL = 6000;

    public static ObjectManager instance;

    private final YmlStorage storage;

    private final Map<String, BattlePoint> battlePoints = new LinkedHashMap<String, BattlePoint>();
    private final Map<String, PvpPlayer> pvpPlayers = new ConcurrentHashMap<String, PvpPlayer>();
    private final Set<String> unloadSet = Collections.synchronizedSet(new HashSet<String>());

    public static void init(PvpRealm plugin) {
        instance = new ObjectManager(plugin);
    }

    private ObjectManager(PvpRealm plugin) {
        storage = new YmlStorage(plugin.getDataFolder());

        List<BattlePoint> battlePointsList = storage.loadBattlePoints();
        for (BattlePoint battlePoint : battlePointsList) {
            battlePoints.put(battlePoint.getName(), battlePoint);
        }

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new AutoSaveTask(), AUTOSAVE_INTERVAL, AUTOSAVE_INTERVAL);
        Bukkit.getPluginManager().registerEvents(new UnloadMarker(), plugin);
    }

    public PvpPlayer getPvpPlayer(Player player) {
        PvpPlayer result = pvpPlayers.get(player.getName());
        if (result == null) {
            result = storage.loadPvpPlayer(player);
            if (result == null) {
                result = new PvpPlayer(player);
            }
            pvpPlayers.put(result.getName(), result);
        }
        return result;
    }

    public BattlePoint getBattlePoint(String name) {
        return battlePoints.get(name.toLowerCase());
    }

    public BattlePoint[] getBattlePoints() {
        return battlePoints.values().toArray(new BattlePoint[battlePoints.size()]);
    }

    public boolean removeBattlePoint(String bPointName) {
        boolean removed = battlePoints.remove(bPointName.toLowerCase()) != null;
        if (removed) {
            storage.storeBattlePoints(battlePoints.values());
        }
        return removed;
    }

    public void addBattlePoint(BattlePoint newPoint) {
        battlePoints.put(newPoint.getName(), newPoint);
        storage.storeBattlePoints(battlePoints.values());
    }

    public void shutdown() {
        Config.save();
        storage.storeBattlePoints(battlePoints.values());
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
                if (unloadSet.remove(pvpPlayer.getName())) {
                    itr.remove();
                }
            }
        }
    }

    private class UnloadMarker implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            PvpPlayer pvpPlayer = pvpPlayers.get(event.getPlayer().getName());
            if (pvpPlayer != null) {
                unloadSet.add(pvpPlayer.getName());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            String playerName = event.getPlayer().getName();

            unloadSet.remove(playerName);
            PvpPlayer joined = pvpPlayers.get(playerName);
            if (joined != null) {
                joined.updateEntity(event.getPlayer());
            }
        }
    }
}
