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

    private boolean usingTowny = false;
    private TownyFacade towny;

    @Override
    public void onEnable() {
        GUtils.init(getLogger(), "PvpRealm");
        loadConfig();
        ObjectManager.init(this);
        initDependencies();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PvpRealmEventHandler(this), this);
        pm.registerEvents(new ScrollHandler(this), this);
        PvpRealmCommandExecutor commandExecutor = new PvpRealmCommandExecutor(this);
        getCommand("pvprealm").setExecutor(commandExecutor);

        GUtils.log("Pvp Realm enabled");
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
        ObjectManager.instance.shutdown();
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
