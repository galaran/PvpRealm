package mccity.plugins.pvprealm.persistence;

import mccity.plugins.pvprealm.Settings;
import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.IOUtils;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class YmlStorage {

    private final PvpRealm plugin;

    private final File playersDir;

    private final File battlePointsFile;

    public YmlStorage(PvpRealm plugin) {
        this.plugin = plugin;
        File pluginDir = plugin.getDataFolder();
        playersDir = new File(pluginDir, "players");
        if (!playersDir.isDirectory()) {
            playersDir.mkdirs();
        }

        battlePointsFile = new File(pluginDir, "battle_points.yml");
        IOUtils.createFileIfNotExists(battlePointsFile);
    }

    public List<BattlePoint> loadBattlePoints() {
        FileConfiguration bpsRoot = YamlConfiguration.loadConfiguration(battlePointsFile);
        Set<String> keys = bpsRoot.getKeys(false);

        List<BattlePoint> results = new ArrayList<BattlePoint>();
        for (String key : keys) {
            results.add(new BattlePoint(bpsRoot.getConfigurationSection(key)));
        }
        return results;
    }

    public void storeBattlePoints(Collection<BattlePoint> points) {
        FileConfiguration bpsRoot = new YamlConfiguration();

        int idx = 0;
        for (BattlePoint curPoint : points) {
            bpsRoot.set(String.valueOf(idx++), curPoint.serialize());
        }

        saveYml(bpsRoot, battlePointsFile);
    }

    public PvpPlayer loadPvpPlayer(Player player) {
        File playerProfile = getPlayerFile(player.getName());
        if (playerProfile.isFile()) {
            FileConfiguration playerRoot = YamlConfiguration.loadConfiguration(playerProfile);
            PvpPlayer pvpPlayer = new PvpPlayer(plugin, player);
            pvpPlayer.deserialize(playerRoot.getConfigurationSection(player.getName()));
            if (Settings.debug) {
                Messaging.log("Pvp player $1 loaded (id: $2)", pvpPlayer.getName(), player.getEntityId());
            }
            return pvpPlayer;
        } else {
            return null;
        }
    }

    public void storePvpPlayer(PvpPlayer pvpPlayer) {
        FileConfiguration playerRoot = new YamlConfiguration();
        playerRoot.set(pvpPlayer.getName(), pvpPlayer.serialize());
        saveYml(playerRoot, getPlayerFile(pvpPlayer.getName()));
        if (Settings.debug) {
            Messaging.log("Pvp player $1 stored", pvpPlayer.getName());
        }
    }

    private File getPlayerFile(String name) {
        File subDir = new File(playersDir, name.substring(0, 1).toLowerCase());
        return new File(subDir, name + ".yml");
    }

    private void saveYml(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException ex) {
            Messaging.log(Level.SEVERE, "Failed to save " + file.getAbsolutePath());
            ex.printStackTrace();
        }
    }
}
