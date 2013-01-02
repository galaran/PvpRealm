package me.galaran.bukkitutils.pvprealm;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class IOUtils {

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
    
    public static File firstAvailableNumberedFile(File dir, String fileName) {
        if (!dir.exists()) dir.mkdirs();
        
        String name, dotExt;
        int extDelimiterPos = fileName.lastIndexOf('.');
        if (extDelimiterPos != -1) {
            name = fileName.substring(0, extDelimiterPos);
            dotExt = fileName.substring(extDelimiterPos);
        } else {
            name = fileName;
            dotExt = "";
        }
        
        int index = 0;
        File result;
        do {
            result = new File(dir, name + (++index) + dotExt);
        } while (result.exists());
        
        return result;
    } 
}
