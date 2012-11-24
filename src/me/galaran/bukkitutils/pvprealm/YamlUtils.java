package me.galaran.bukkitutils.pvprealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlUtils {

    public static Map<String, Object> serializeLocation(Location loc) {
        if (loc == null) return null;

        Map<String, Object> locData = new LinkedHashMap<String, Object>();
        locData.put("x", loc.getX());
        locData.put("y", loc.getY());
        locData.put("z", loc.getZ());
        locData.put("world", loc.getWorld().getName());
        locData.put("pitch", loc.getPitch());
        locData.put("yaw", loc.getYaw());
        return locData;
    }

    public static Location deserializeLocation(Object locDataObject) {
        if (locDataObject == null) return null;
        Map<?, ?> locData = (Map<?, ?>) locDataObject;

        World world = Bukkit.getServer().getWorld((String) locData.get("world"));
        if (world == null) {
            throw new IllegalArgumentException("Non-existent world: " + locData.get("world"));
        }
        Location loc = new Location(world,
                ((Number) locData.get("x")).doubleValue(),
                ((Number) locData.get("y")).doubleValue(),
                ((Number) locData.get("z")).doubleValue());

        if (locData.containsKey("pitch")) {
            loc.setPitch(((Number) locData.get("pitch")).floatValue());
        }
        if (locData.containsKey("yaw")) {
            loc.setYaw(((Number) locData.get("yaw")).floatValue());
        }

        return loc;
    }

    public static void saveYml(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void createDirectoryPathIfNotExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
