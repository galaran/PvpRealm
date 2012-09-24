package mccity.plugins.pvprealm;

import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import java.io.File;

public class Config {

    public static boolean pvpWorldEnabled;
    public static World pvpWorld;
    public static boolean deathHeroesExpLoss;
    public static Location entryLoc;
    public static Location defaultReturnLoc;

    public static boolean scroll;
    public static MaterialData scrollItem;
    public static boolean consumeScroll;
    public static int scrollDelaySec;

    public static boolean pvpLogger;
    public static boolean pvpLoggerOp;
    public static boolean pvpLoggerBypassFriendly;
    public static boolean pvpLoggerGlobal;
    public static boolean pvpLoggerMessage;
    public static String pvpLoggerMessageText;
    public static int pvpLoggerExpPenalty;
    public static boolean pvpLoggerKill;

    public static boolean load(File configFile) {
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);

        // Pvp World
        ConfigurationSection worldSection = fileConfig.getConfigurationSection("pvp-world");
        pvpWorldEnabled = worldSection.getBoolean("enable", false);
        if (pvpWorldEnabled) {
            String pvpWorldName = worldSection.getString("world");
            pvpWorld = Bukkit.getServer().getWorld(pvpWorldName);
            if (pvpWorld == null) {
                GUtils.log("Pvp World is null, check world name");
                return false;
            }
            deathHeroesExpLoss = worldSection.getBoolean("death-heroes-exp-loss", false);
            entryLoc = GUtils.deserializeLocation(worldSection.getConfigurationSection("entry-loc").getValues(false));
            if (!entryLoc.getWorld().equals(pvpWorld)) {
                GUtils.log("Entry location must be in the pvp world");
                return false;
            }
            defaultReturnLoc = GUtils.deserializeLocation(worldSection.getConfigurationSection("default-return-loc").getValues(false));

            // Enter Scroll
            scroll = worldSection.getBoolean("enter-scroll.enable", false);
            if (scroll) {
                scrollItem = GUtils.parseMatData(worldSection.getString("enter-scroll.item", "369"), "-");
                consumeScroll = worldSection.getBoolean("enter-scroll.consume", true);
                scrollDelaySec = worldSection.getInt("enter-scroll.delay-sec", 10);
            }
        } else {
            scroll = false;
        }

        // Pvp Logger
        ConfigurationSection loggerSection = fileConfig.getConfigurationSection("pvp-logger");

        pvpLogger = loggerSection.getBoolean("enable", false);
        if (pvpLogger) {
            pvpLoggerOp = loggerSection.getBoolean("enable-op", false);
            pvpLoggerBypassFriendly = loggerSection.getBoolean("bypass-friendly", true);
            pvpLoggerGlobal = loggerSection.getBoolean("global", true);
            pvpLoggerMessage = loggerSection.getBoolean("message", true);
            pvpLoggerMessageText = loggerSection.getString("message-text");
            pvpLoggerExpPenalty = loggerSection.getInt("heroes-exp-penalty");
            pvpLoggerKill = loggerSection.getBoolean("kill", false);
        }

        return true;
    }
}
