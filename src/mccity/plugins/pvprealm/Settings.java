package mccity.plugins.pvprealm;

import me.galaran.bukkitutils.pvprealm.DoOrNotify;
import me.galaran.bukkitutils.pvprealm.GUtils;
import me.galaran.bukkitutils.pvprealm.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class Settings {

    public static boolean debug;
    public static String lang;

    public static boolean pvpwEnabled;
    public static World pvpWorld;
    public static Location pvpwEntryLoc;
    public static Location pvpwDefaultReturnLoc;
    public static boolean pvpwDisableHeroesDeathExpLoss;
    public static boolean pvpwDisableHeroesMobExp;
    public static boolean pvpwDisableWeather;

    public static boolean scroll;
    public static MaterialData scrollItem;
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

    public static boolean kitSignsGlobal;

    private static Set<Pair<String, World>> deathNoDropRegions = new HashSet<Pair<String, World>>();

    public static boolean load(File configFile) {
        FileConfiguration root = YamlConfiguration.loadConfiguration(configFile);

        debug = root.getBoolean("debug", false);
        lang = root.getString("lang", "english");

        // Pvp World
        ConfigurationSection worldSection = root.getConfigurationSection("pvp-world");
        pvpwEnabled = worldSection.getBoolean("enable", false);
        if (pvpwEnabled) {
            String pvpWorldName = worldSection.getString("world");
            pvpWorld = DoOrNotify.getWorld(pvpWorldName, Bukkit.getConsoleSender());
            if (pvpWorld == null) {
                return false;
            }
            pvpwEntryLoc = GUtils.deserializeLocation(worldSection.getConfigurationSection("entry-loc").getValues(false));
            if (!pvpwEntryLoc.getWorld().equals(pvpWorld)) {
                GUtils.log("Entry location must be in the Pvp World");
                return false;
            }
            pvpwDefaultReturnLoc = GUtils.deserializeLocation(worldSection.getConfigurationSection("default-return-loc").getValues(false));
            if (pvpwDefaultReturnLoc.getWorld().equals(pvpWorld)) {
                GUtils.log("Entry location must be out of the Pvp World");
                return false;
            }

            pvpwDisableHeroesDeathExpLoss = worldSection.getBoolean("disable-heroes-death-exp-loss", true);
            pvpwDisableHeroesMobExp = worldSection.getBoolean("disable-heroes-mob-exp", true);
            pvpwDisableWeather = worldSection.getBoolean("disable-weather", true);

            // Enter Scroll
            scroll = worldSection.getBoolean("enter-scroll.enable", false);
            if (scroll) {
                scrollItem = GUtils.parseMatData(worldSection.getString("enter-scroll.item", "369"), "-");
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

        kitSignsGlobal = root.getBoolean("kit-signs-global", false);

        List<Map<?, ?>> dndRegions = root.getMapList("death-nodrop-regions");
        for (Map<?, ?> dndRegion : dndRegions) {
            String id = (String) dndRegion.get("id");
            String worldName = (String) dndRegion.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                GUtils.log("dnd region " + id + " skipped: world " + worldName + " not loaded", Level.SEVERE);
            } else {
                Pair<String, World> entry = new Pair<String, World>(id.toLowerCase(), world);
                deathNoDropRegions.add(entry);
            }
        }
        GUtils.log(deathNoDropRegions.size() + " dnd regions");

        return true;
    }

    public static boolean isDndRegion(String id, World world) {
        return deathNoDropRegions.contains(new Pair<String, World>(id, world));
    }
}
