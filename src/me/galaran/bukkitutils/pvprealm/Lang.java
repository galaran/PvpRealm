package me.galaran.bukkitutils.pvprealm;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class Lang {

    private static final Map<String, String> langMap = new HashMap<String, String>();
    private static String lang;

    public static void initLang(String language, Plugin plugin) throws Exception {
        String langFileName = language + ".lang";
        File langFile = new File(plugin.getDataFolder(), langFileName);
        if (!langFile.isFile()) {
            plugin.saveResource(langFileName, false);
            if (!langFile.isFile()) throw new FileNotFoundException("No lang file for " + language);
        }

        Properties prop = new Properties();
        prop.load(new InputStreamReader(new FileInputStream(langFile), "utf-8"));

        lang = language;
        langMap.clear();
        for (String curKey : prop.stringPropertyNames()) {
            langMap.put(curKey, prop.getProperty(curKey));
        }

        updateUtilsTranslation();
    }

    private static void updateUtilsTranslation() {
        DoOrNotify.setNotifyMessages(langMap.get("utils.no-player"), langMap.get("utils.no-world"), langMap.get("utils.cs-not-player"));
    }

    public static String getTranslation(String key) {
        String val = langMap.get(key);
        if (val == null) {
            val = ChatColor.RED + "Missing translation for key " + ChatColor.DARK_RED + key + ChatColor.RED + ", lang: " + lang;
            GUtils.log(Level.WARNING, val);
        }
        return val;
    }
}
