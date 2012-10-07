package mccity.plugins.pvprealm.object;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemsKit implements ConfigurationSerializable {

    private final String name;
    private final List<ItemStack> stacks = new ArrayList<ItemStack>();

    /**
     * @param content may contains null values
     */
    public ItemsKit(String name, List<ItemStack> content) {
        this.name = name;
        for (ItemStack curStack : content) {
            if (curStack == null || curStack.getType() == Material.AIR) continue;
            stacks.add(curStack.clone());
        }
    }

    /**
     * @param content may contains null values
     */
    public ItemsKit(String name, ItemStack[] content) {
        this(name, Arrays.asList(content));
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getContent() {
        List<ItemStack> deepCopy = new ArrayList<ItemStack>();
        for (ItemStack stack : stacks) {
            deepCopy.add(stack.clone());
        }
        return deepCopy;
    }

    public ItemStack[] getContentArray() {
        List<ItemStack> result = getContent();
        return result.toArray(new ItemStack[result.size()]);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", name);

        List<Map<String, Object>> stacksData = new ArrayList<Map<String, Object>>();
        for (ItemStack curStack : stacks) {
            stacksData.add(curStack.serialize());
        }
        result.put("content", stacksData);

        return result;
    }

    @SuppressWarnings("unchecked")
    public ItemsKit(ConfigurationSection section) {
        name = section.getString("name");

        List<Map<?, ?>> curKitData = section.getMapList("content");
        for (Map<?, ?> stackData : curKitData) {
            stacks.add(ItemStack.deserialize((Map<String, Object>) stackData));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
