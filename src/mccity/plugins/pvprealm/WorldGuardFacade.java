package mccity.plugins.pvprealm;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardFacade {

    private final WorldGuardPlugin wgPlugin;

    public WorldGuardFacade(WorldGuardPlugin worldGuardPlugin) {
        this.wgPlugin = worldGuardPlugin;
    }

    /**
     * @return low case regions id at location
     */
    public List<String> getRegions(Location loc) {
        List<String> applicableRegions = new ArrayList<String>();
        RegionManager rgMan = wgPlugin.getRegionManager(loc.getWorld());
        for (ProtectedRegion region : rgMan.getApplicableRegions(loc)) {
            applicableRegions.add(region.getId().toLowerCase());
        }
        return applicableRegions;
    }
}
