package mccity.plugins.pvprealm;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import mccity.plugins.pvprealm.command.PvpRealmCommandExecutor;
import mccity.plugins.pvprealm.listeners.DeathNoDropListener;
import mccity.plugins.pvprealm.listeners.PvpLoggerListener;
import mccity.plugins.pvprealm.listeners.PvpRealmListener;
import mccity.plugins.pvprealm.listeners.ScrollListener;
import mccity.plugins.pvprealm.object.ObjectManager;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import me.galaran.bukkitutils.pvprealm.text.TranslationLang;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PvpRealm extends JavaPlugin {

    private TranslationLang translation;

    private Heroes heroes;

    private boolean usingTowny = false;
    private TownyFacade towny;

    private boolean usingWorldGuard = false;
    private WorldGuardFacade worldGuard;

    @Override
    public void onEnable() {
        translation = new TranslationLang(this, "english");
        Messaging.init(getLogger(), ChatColor.GRAY + "[PvpRealm] ", translation);

        reloadSettings();
        ObjectManager.init(this);
        initDependencies();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PvpRealmListener(this), this);
        pm.registerEvents(new PvpLoggerListener(this), this);
        pm.registerEvents(new ScrollListener(this), this);
        if (usingWorldGuard) {
            pm.registerEvents(new DeathNoDropListener(this), this);
        }
        PvpRealmCommandExecutor commandExecutor = new PvpRealmCommandExecutor(this);
        getCommand("pvprealm").setExecutor(commandExecutor);

        Messaging.log("Pvp Realm " + getDescription().getVersion() + " enabled");
    }

    public boolean reloadSettings() {
        File configFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
        
        boolean settingsOk = Settings.reload(configFile);
        Messaging.setDebug(Settings.debug);
        translation.reload(Settings.lang);
        
        return settingsOk;
    }

    @Override
    public void onDisable() {
        ObjectManager.instance.shutdown();
        Messaging.log("Pvp Realm disabled");
    }

    private void initDependencies() {
        PluginManager pm = getServer().getPluginManager();
        heroes = (Heroes) pm.getPlugin("Heroes");

        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin instanceof Towny) {
            towny = new TownyFacade((Towny) townyPlugin);
            usingTowny = true;
            Messaging.log("Linked with Towny");
        }

        Plugin worldGuardPlugin = pm.getPlugin("WorldGuard");
        if (worldGuardPlugin != null && worldGuardPlugin instanceof WorldGuardPlugin) {
            worldGuard = new WorldGuardFacade((WorldGuardPlugin) worldGuardPlugin);
            usingWorldGuard = true;
            Messaging.log("Linked with WorldGuard");
        }
    }

    public Hero getHero(PvpPlayer pvpPlayer) {
        return heroes.getCharacterManager().getHero(pvpPlayer.getPlayer());
    }

    public boolean isUsingTowny() {
        return usingTowny;
    }

    public TownyFacade getTowny() {
        if (usingTowny) {
            return towny;
        } else {
            throw new RuntimeException("Not linked with Towny");
        }
    }

    public WorldGuardFacade getWorldGuard() {
        if (usingWorldGuard) {
            return worldGuard;
        } else {
            throw new RuntimeException("Not linked with WorldGuard");
        }
    }
}
