package mccity.plugins.pvprealm.object;

import me.galaran.bukkitutils.pvprealm.LocUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BattlePoint implements ConfigurationSerializable {

    private final Location loc;
    private final String name;

    public BattlePoint(Location loc, String name) {
        this.loc = loc;
        this.name = name;
    }

    public BattlePoint(ConfigurationSection section) {
        name = section.getString("name");
        loc = LocUtils.deserialize(section.getConfigurationSection("location"));
    }

    public Location getLoc() {
        return loc;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", name);
        result.put("location", LocUtils.serialize(loc));
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
