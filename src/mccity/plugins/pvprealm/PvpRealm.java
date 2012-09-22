package mccity.plugins.pvprealm;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.palmergames.bukkit.towny.Towny;
import mccity.plugins.pvprealm.command.PvpRealmCommandExecutor;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PvpRealm extends JavaPlugin {

    private Heroes heroesPlugin;
    private boolean enabled = false;

    private TownyFacade towny;
    private boolean usingTowny = false;

    @Override
    public void onEnable() {
        GUtils.init(getLogger(), "PvpRealm");
        loadConfig();
        ObjectManager.init(this);
        initDependencies();

        getServer().getPluginManager().registerEvents(new PvpRealmEventHandler(this), this);
        PvpRealmCommandExecutor commandExecutor = new PvpRealmCommandExecutor(this);
        getCommand("pvprealm").setExecutor(commandExecutor);

        enabled = true;
        GUtils.log("Pvp Realm enabled. World: " + Config.pvpWorld.getName());
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

    private void initDependencies() {
        PluginManager pm = getServer().getPluginManager();
        heroesPlugin = (Heroes) pm.getPlugin("Heroes");

        Towny townyplugin = (Towny) pm.getPlugin("Towny");
        if (townyplugin != null) {
            towny = new TownyFacade(townyplugin.getTownyUniverse());
            usingTowny = true;
            GUtils.log("Linked with Towny");
        }
    }

    public Hero getHero(PvpPlayer pvpPlayer) {
        return heroesPlugin.getCharacterManager().getHero(pvpPlayer.getPlayer());
    }

    public boolean isUsingTowny() {
        return usingTowny;
    }

    public TownyFacade getTowny() {
        if (usingTowny) {
            return towny;
        } else {
            throw new RuntimeException("Not linked with towny");
        }
    }
}
