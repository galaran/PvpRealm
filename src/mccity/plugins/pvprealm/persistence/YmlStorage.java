package mccity.plugins.pvprealm.persistence;

import mccity.plugins.pvprealm.object.BattlePoint;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class YmlStorage {

    private final File playersDir;

    private final File battlePointsFile;
    private final String BATTLE_POINTS_ROOT = "battlePoints";

    public YmlStorage(File pluginDir) {
        playersDir = new File(pluginDir, "players");
        if (!playersDir.isDirectory()) {
            playersDir.mkdirs();
        }
        battlePointsFile = new File(pluginDir, "battle_points.yml");
        if (!battlePointsFile.isFile()) {
            try {
                battlePointsFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<BattlePoint> loadBattlePoints() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(battlePointsFile);
        List<Map<?, ?>> battlePointsData = config.getMapList(BATTLE_POINTS_ROOT);

        List<BattlePoint> results = new ArrayList<BattlePoint>();
        if (battlePointsData != null) {
            for (Map<?, ?> curPointData : battlePointsData) {
                BattlePoint curBattlePoint = new BattlePoint(curPointData);
                results.add(curBattlePoint);
            }
        }
        return results;
    }

    public void storeBattlePoints(Collection<BattlePoint> points) {
        FileConfiguration config = new YamlConfiguration();

        List<Map<String, ?>> battlePointsData = new ArrayList<Map<String, ?>>();
        for (BattlePoint curPoint : points) {
            battlePointsData.add(curPoint.serialize());
        }

        config.set(BATTLE_POINTS_ROOT, battlePointsData);
        saveYml(config, battlePointsFile);
    }

    public PvpPlayer loadPvpPlayer(Player player) {
        File playerProfile = getPlayerFile(player.getName());
        if (playerProfile.isFile()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerProfile);
            PvpPlayer pvpPlayer = new PvpPlayer(player);
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

    private void saveYml(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException ex) {
            GUtils.log("Failed to save " + file.getAbsolutePath(), Level.SEVERE);
            ex.printStackTrace();
        }
    }
}
