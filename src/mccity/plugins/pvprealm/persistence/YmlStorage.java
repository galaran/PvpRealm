package mccity.plugins.pvprealm.persistence;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.ItemsKit;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
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
    private final File kitsFile;

    public YmlStorage(PvpRealm plugin) {
        this.plugin = plugin;
        File pluginDir = plugin.getDataFolder();
        playersDir = new File(pluginDir, "players");
        if (!playersDir.isDirectory()) {
            playersDir.mkdirs();
        }

        battlePointsFile = new File(pluginDir, "battle_points.yml");
        createFileIfNotExists(battlePointsFile);

        kitsFile = new File(pluginDir, "kits.yml");
        createFileIfNotExists(kitsFile);
    }

    public List<BattlePoint> loadBattlePoints() {
        FileConfiguration bPointsConfig = YamlConfiguration.loadConfiguration(battlePointsFile);
        Set<String> keys = bPointsConfig.getKeys(false);

        List<BattlePoint> results = new ArrayList<BattlePoint>();
        for (String key : keys) {
            results.add(new BattlePoint(bPointsConfig.getConfigurationSection(key)));
        }
        return results;
    }

    public void storeBattlePoints(Collection<BattlePoint> points) {
        FileConfiguration config = new YamlConfiguration();

        int idx = 0;
        for (BattlePoint curPoint : points) {
            config.set(String.valueOf(idx++), curPoint.serialize());
        }

        saveYml(config, battlePointsFile);
    }

    public PvpPlayer loadPvpPlayer(Player player) {
        File playerProfile = getPlayerFile(player.getName());
        if (playerProfile.isFile()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerProfile);
            PvpPlayer pvpPlayer = new PvpPlayer(plugin, player);
            pvpPlayer.load(config.getConfigurationSection(player.getName()));
            return pvpPlayer;
        } else {
            return null;
        }
    }

    public void storePvpPlayer(PvpPlayer pvpPlayer) {
        FileConfiguration config = new YamlConfiguration();
        config.set(pvpPlayer.getName(), pvpPlayer.serialize());
        saveYml(config, getPlayerFile(pvpPlayer.getName()));
    }

    private File getPlayerFile(String name) {
        File subDir = new File(playersDir, name.substring(0, 1).toLowerCase());
        return new File(subDir, name + ".yml");
    }

    public List<ItemsKit> loadKits() {
        FileConfiguration kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        Set<String> keys = kitsConfig.getKeys(false);

        List<ItemsKit> results = new ArrayList<ItemsKit>();
        for (String curKey : keys) {
            results.add(new ItemsKit(kitsConfig.getConfigurationSection(curKey)));
        }
        return results;
    }

    public void storeKits(Collection<ItemsKit> kits) {
        FileConfiguration config = new YamlConfiguration();

        int idx = 0;
        for (ItemsKit kit : kits) {
            config.set(String.valueOf(idx++), kit.serialize());
        }

        saveYml(config, kitsFile);
    }

    private void saveYml(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException ex) {
            GUtils.log("Failed to save " + file.getAbsolutePath(), Level.SEVERE);
            ex.printStackTrace();
        }
    }

    private void createFileIfNotExists(File file) {
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
