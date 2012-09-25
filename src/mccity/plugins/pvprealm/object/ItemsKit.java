package mccity.plugins.pvprealm.object;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemsKit implements ConfigurationSerializable {

    private final String name;
    private final ItemStack[] stacks;

    public ItemsKit(String name, ItemStack[] slots) {
        this.name = name;

        List<ItemStack> content = new ArrayList<ItemStack>();
        for (ItemStack curSlot : slots) {
            if (curSlot == null) continue;
            content.add(curSlot);
        }
        stacks = content.toArray(new ItemStack[content.size()]);
    }

    public ItemStack[] getStacks() {
        return stacks;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        List<Map<String, Object>> stacksData = new ArrayList<Map<String, Object>>();

        for (ItemStack curStack : stacks) {
            stacksData.add(curStack.serialize());
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", name);
        result.put("content", stacksData);
        return result;
    }

    public ItemsKit(ConfigurationSection section) {
        name = section.getString("name");

        List<Map<?, ?>> curKitData = section.getMapList("content");
        stacks = new ItemStack[curKitData.size()];
        int idx = 0;
        for (Map<?, ?> stackData : curKitData) {
            stacks[idx++] = ItemStack.deserialize((Map<String, Object>) stackData);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
