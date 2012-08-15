package mccity.plugins.pvprealm;

import mccity.plugins.pvprealm.command.PvpRealmCommandExecutor;
import mccity.plugins.pvprealm.object.ObjectManager;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class PvpRealm extends JavaPlugin {

    private boolean enabled = false;

    @Override
    public void onEnable() {
        GUtils.init(getLogger(), "PvpRealm");

        File configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            saveDefaultConfig();
        }

        try {
            Config.create(configFile);
        } catch (IllegalWorldException ex) {
            GUtils.log("Pvp world not loaded. Plugin not enabled.", Level.SEVERE);
            return;
        }

        ObjectManager.init(this);

        getServer().getPluginManager().registerEvents(new PvpRealmEventHandler(this), this);

        PvpRealmCommandExecutor commandExecutor = new PvpRealmCommandExecutor();
        getCommand("pvprealm").setExecutor(commandExecutor);

        GUtils.log("Pvp Realm enabled. World: " + Config.getPvpWorld().getName());
        enabled = true;
    }

    @Override
    public void onDisable() {
        if (enabled) {
            ObjectManager.instance.shutdown();
        }
        GUtils.log("Pvp Realm disabled");
    }
}
