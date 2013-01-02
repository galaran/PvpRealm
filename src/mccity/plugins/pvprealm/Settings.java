package mccity.plugins.pvprealm;

import me.galaran.bukkitutils.pvprealm.IdData;
import me.galaran.bukkitutils.pvprealm.LocUtils;
import me.galaran.bukkitutils.pvprealm.Pair;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Settings {

    static boolean debug;
    static String lang;

    public static boolean pvpwEnabled;
    public static World pvpWorld;
    public static Location pvpwEntryLoc;
    public static Location pvpwDefaultReturnLoc;
    public static boolean pvpwDisableHeroesDeathExpLoss;
    public static boolean pvpwDisableHeroesMobExp;
    public static boolean pvpwDisableWeather;

    public static boolean scroll;
    public static IdData scrollItem;
    public static boolean consumeScroll;
    public static int scrollDelaySec;
    public static boolean scrollBroadcastArrival;

    public static boolean pvpLogger;
    public static boolean pvpLoggerOp;
    public static boolean pvpLoggerBypassFriendly;
    public static boolean pvpLoggerGlobal;
    public static boolean pvpLoggerMessage;
    public static int pvpLoggerExpPenalty;
    public static boolean pvpLoggerKill;

    private static final Set<Pair<String, World>> deathNoDropRegions = new HashSet<Pair<String, World>>();

    static boolean reload(File configFile) {
        FileConfiguration root = YamlConfiguration.loadConfiguration(configFile);

        debug = root.getBoolean("debug", false);
        lang = root.getString("lang", "english");

        // Pvp World
        ConfigurationSection worldSection = root.getConfigurationSection("pvp-world");
        pvpwEnabled = worldSection.getBoolean("enable", false);
        if (pvpwEnabled) {
            String pvpWorldName = worldSection.getString("world");
            pvpWorld = Messaging.getWorld(pvpWorldName, Bukkit.getConsoleSender());
            if (pvpWorld == null) {
                return false;
            }
            pvpwEntryLoc = LocUtils.deserialize(worldSection.getConfigurationSection("entry-loc"));
            if (!pvpwEntryLoc.getWorld().equals(pvpWorld)) {
                Messaging.log("Entry location must be in the Pvp World");
                return false;
            }
            pvpwDefaultReturnLoc = LocUtils.deserialize(worldSection.getConfigurationSection("default-return-loc"));
            if (pvpwDefaultReturnLoc.getWorld().equals(pvpWorld)) {
                Messaging.log("Entry location must be out of the Pvp World");
                return false;
            }

            pvpwDisableHeroesDeathExpLoss = worldSection.getBoolean("disable-heroes-death-exp-loss", true);
            pvpwDisableHeroesMobExp = worldSection.getBoolean("disable-heroes-mob-exp", true);
            pvpwDisableWeather = worldSection.getBoolean("disable-weather", true);

            // Enter Scroll
            scroll = worldSection.getBoolean("enter-scroll.enable", false);
            if (scroll) {
                scrollItem = IdData.deserialize(worldSection.getString("enter-scroll.item", "369"));
                consumeScroll = worldSection.getBoolean("enter-scroll.consume", true);
                scrollDelaySec = worldSection.getInt("enter-scroll.delay-sec", 10);
                scrollBroadcastArrival = worldSection.getBoolean("enter-scroll.broadcast-arrival", true);
            }
        } else {
            scroll = false;
        }

        // Pvp Logger
        ConfigurationSection loggerSection = root.getConfigurationSection("pvp-logger");

        pvpLogger = loggerSection.getBoolean("enable", false);
        if (pvpLogger) {
            pvpLoggerOp = loggerSection.getBoolean("enable-op", false);
            pvpLoggerBypassFriendly = loggerSection.getBoolean("bypass-friendly", true);
            pvpLoggerGlobal = loggerSection.getBoolean("global", true);
            pvpLoggerMessage = loggerSection.getBoolean("message", true);
            pvpLoggerExpPenalty = loggerSection.getInt("heroes-exp-penalty");
            pvpLoggerKill = loggerSection.getBoolean("kill", false);
        }

        deathNoDropRegions.clear();
        List<Map<?, ?>> dndRegions = root.getMapList("death-nodrop-regions");
        for (Map<?, ?> dndRegion : dndRegions) {
            String id = (String) dndRegion.get("id");
            String worldName = (String) dndRegion.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                Messaging.log(Level.SEVERE, "dnd region " + id + " skipped: world " + worldName + " not loaded");
            } else {
                Pair<String, World> entry = new Pair<String, World>(id.toLowerCase(), world);
                deathNoDropRegions.add(entry);
            }
        }
        Messaging.log(deathNoDropRegions.size() + " dnd regions");

        return true;
    }

    public static boolean isDndRegion(String id, World world) {
        return deathNoDropRegions.contains(new Pair<String, World>(id, world));
    }
}
