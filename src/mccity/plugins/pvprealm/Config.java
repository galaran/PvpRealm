package mccity.plugins.pvprealm;

import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class Config {

    public static World pvpWorld;

    public static Location entryLoc;
    public static Location defaultReturnLoc;
    public static boolean deathHeroesExpLoss;

    public static boolean pvpLogger;
    public static boolean pvpLoggerGlobal;
    public static boolean pvpLoggerMessage;
    public static String pvpLoggerMessageText;
    public static int pvpLoggerExpPenalty;
    public static boolean pvpLoggerKill;

    public static boolean load(File configFile) {
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection worldSection = fileConfig.getConfigurationSection("pvp-world");

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

        ConfigurationSection loggerSection = fileConfig.getConfigurationSection("pvp-logger");

        pvpLogger = loggerSection.getBoolean("enable", true);
        pvpLoggerGlobal = loggerSection.getBoolean("global", true);
        pvpLoggerMessage = loggerSection.getBoolean("message", true);
        pvpLoggerMessageText = loggerSection.getString("message-text");
        pvpLoggerExpPenalty = loggerSection.getInt("heroes-exp-penalty");
        pvpLoggerKill = loggerSection.getBoolean("kill", false);

        return true;
    }
}
