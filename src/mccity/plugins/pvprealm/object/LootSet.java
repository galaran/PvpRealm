package mccity.plugins.pvprealm.object;

import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LootSet implements ConfigurationSerializable {

    private final String name;
    private final Map<String, Float> chancedKits = new LinkedHashMap<String, Float>();

    public LootSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Float> getKitMapping() {
        return chancedKits;
    }

    public ItemsKit rollKit() {
        ObjectManager om = ObjectManager.instance;
        List<ItemStack> resultStacks = new ArrayList<ItemStack>();
        for (Map.Entry<String, Float> kitEntry : chancedKits.entrySet()) {
            String kitName = kitEntry.getKey();
            ItemsKit kit = om.getKit(kitName);
            if (kit == null) {
                GUtils.log(Level.WARNING, "Kit $1 from lootset $2 not exists", kitName, name);
                continue;
            }

            float kitChance = kitEntry.getValue();
            if (GUtils.random.nextFloat() < kitChance) {
                for (ItemStack curStack : kit.getContent()) {
                    resultStacks.add(curStack);
                }
            }
        }
        return new ItemsKit(name + "*", resultStacks);
    }

    public LootSet(ConfigurationSection section) {
        name = section.getString("name");

        for (Map<?, ?> kitData : section.getMapList("kits")) {
            chancedKits.put((String) kitData.get("kitname"), ((Number) kitData.get("chance")).floatValue());
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", name);

        List<Map<String, Object>> kitsData = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Float> curKitEntry : chancedKits.entrySet()) {
            Map<String, Object> curKitData = new LinkedHashMap<String, Object>();
            curKitData.put("kitname", curKitEntry.getKey());
            curKitData.put("chance", curKitEntry.getValue());
            kitsData.add(curKitData);
        }
        result.put("kits", kitsData);

        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
