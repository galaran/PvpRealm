package me.galaran.bukkitutils.pvprealm;

import com.google.common.base.Splitter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Iterator;

public class IdData {

    private final int id;
    private final short data;

    public IdData(int id, short data) {
        this.id = id;
        this.data = data;
    }

    public IdData(int id) {
        this(id, (short) 0);
    }

    public IdData(Material mat) {
        this(mat.getId());
    }

    public IdData(Material mat, short data) {
        this(mat.getId(), data);
    }

    public IdData(MaterialData matData) {
        this(matData.getItemType(), matData.getData());
    }

    public IdData(ItemStack stack) {
        this(stack.getType(), stack.getDurability());
    }

    public IdData(Block block) {
        this(block.getType(), block.getData());
    }

    public boolean matches(MaterialData matData) {
        if (matData == null) return false;
        return id == matData.getItemTypeId() && data == (short) matData.getData();
    }

    public boolean matches(ItemStack stack) {
        if (stack == null) return false;
        return id == stack.getTypeId() && data == stack.getDurability();
    }

    public boolean matches(Block block) {
        if (block == null) return false;
        return id == block.getTypeId() && data == (short) block.getData();
    }

    public void apply(Block block, boolean applyPhysics) {
        block.setTypeIdAndData(id, (byte) data, applyPhysics);
    }

    public static IdData parse(String string, String delimiter) {
        if (string == null || string.isEmpty()) return null;

        try {
            if (string.contains(delimiter)) {
                Iterator<String> itr = Splitter.on(delimiter).split(string).iterator();
                return new IdData(Integer.parseInt(itr.next()), Short.parseShort(itr.next()));
            } else {
                return new IdData(Integer.parseInt(string));
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        if (data != 0) {
            sb.append('-');
            sb.append(data);
        }
        return sb.toString();
    }

    public static IdData deserialize(String string) {
        return parse(string, "-");
    }

    public Material toMaterial() {
        return Material.getMaterial(id);
    }

    public MaterialData toMaterialData() {
        return new MaterialData(toMaterial(), (byte) data);
    }

    public int getId() {
        return id;
    }

    public short getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdData idData = (IdData) o;

        if (data != idData.data) return false;
        return id == idData.id;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (int) data;
        return result;
    }
}
