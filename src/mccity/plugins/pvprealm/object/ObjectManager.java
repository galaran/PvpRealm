package mccity.plugins.pvprealm.object;

import mccity.plugins.pvprealm.Config;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.persistence.YmlStorage;
import me.galaran.bukkitutils.pvprealm.GUtils;
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

    private final Map<String, BattlePoint> battlePoints = new LinkedHashMap<String, BattlePoint>();

    private final Map<String, PvpPlayer> pvpPlayers = new ConcurrentHashMap<String, PvpPlayer>();
    private final Set<String> unloadPlayers = Collections.synchronizedSet(new HashSet<String>());

    private final Map<String, ItemsKit> kits = new LinkedHashMap<String, ItemsKit>();

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
        GUtils.log(loadedPoints.size() + " battle points");

        List<ItemsKit> loadedKits = storage.loadKits();
        for (ItemsKit loadedKit : loadedKits) {
            kits.put(loadedKit.getName(), loadedKit);
        }
        GUtils.log(loadedKits.size() + " kits");

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new AutoSaveTask(), AUTOSAVE_PERIOD, AUTOSAVE_PERIOD);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), plugin);
    }

    public PvpPlayer getPvpPlayer(Player player) {
        PvpPlayer result = pvpPlayers.get(player.getName());
        if (result == null) {
            result = storage.loadPvpPlayer(player);
            if (result == null) {
                result = new PvpPlayer(plugin, player);
                if (Config.debug) {
                    GUtils.log("New Pvp player: $1", result.getName());
                }
            }
            pvpPlayers.put(result.getName(), result);
        }
        return result;
    }

    public BattlePoint getBattlePoint(String name) {
        return battlePoints.get(name);
    }

    public List<BattlePoint> getBattlePoints() {
        return new ArrayList<BattlePoint>(battlePoints.values());
    }

    public boolean removeBattlePoint(String bPointName) {
        boolean removed = battlePoints.remove(bPointName) != null;
        if (removed) {
            storage.storeBattlePoints(battlePoints.values());
        }
        return removed;
    }

    public void addBattlePoint(BattlePoint newPoint) {
        battlePoints.put(newPoint.getName(), newPoint);
        storage.storeBattlePoints(battlePoints.values());
    }

    public void addKit(ItemsKit newKit) {
        kits.put(newKit.getName(), newKit);
        storage.storeKits(kits.values());
    }

    public boolean removeKit(String kitName) {
        boolean removed = kits.remove(kitName) != null;
        if (removed) {
            storage.storeKits(kits.values());
        }
        return removed;
    }

    public List<ItemsKit> getKits() {
        return new ArrayList<ItemsKit>(kits.values());
    }

    public ItemsKit getKit(String kitName) {
        return kits.get(kitName);
    }

    public void shutdown() {
        storage.storeBattlePoints(battlePoints.values());
        storage.storeKits(kits.values());
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
            }
        }
    }
}
