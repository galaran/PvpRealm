package mccity.plugins.pvprealm;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import mccity.plugins.pvprealm.command.PvpRealmCommandExecutor;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PvpRealm extends JavaPlugin {

    private Heroes heroesPlugin;
    private boolean enabled = false;

    @Override
    public void onEnable() {
        GUtils.init(getLogger(), "PvpRealm");

        loadConfig();

        ObjectManager.init(this);
        heroesPlugin = (Heroes) getServer().getPluginManager().getPlugin("Heroes");

        getServer().getPluginManager().registerEvents(new PvpRealmEventHandler(this), this);
        PvpRealmCommandExecutor commandExecutor = new PvpRealmCommandExecutor(this);
        getCommand("pvprealm").setExecutor(commandExecutor);



        GUtils.log("Pvp Realm enabled. World: " + Config.pvpWorld.getName());
        enabled = true;
    }

    public boolean loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            saveDefaultConfig();
        }
        return Config.load(configFile);
    }

    @Override
    public void onDisable() {
        if (enabled) {
            ObjectManager.instance.shutdown();
        }
        GUtils.log("Pvp Realm disabled");
    }

    public Hero getHero(PvpPlayer pvpPlayer) {
        return heroesPlugin.getCharacterManager().getHero(pvpPlayer.getPlayer());
    }
}
