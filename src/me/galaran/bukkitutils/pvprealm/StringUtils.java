package me.galaran.bukkitutils.pvprealm;

import org.bukkit.ChatColor;

import java.util.Iterator;

public class StringUtils {

    public static String join(Iterable<String> collection, String delimiter, ChatColor color) {
        return join(collection, color, delimiter, color);
    }

    public static String join(Iterable<String> collection, ChatColor elementColor, String delimiter, ChatColor delimiterColor) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = collection.iterator();
        while (itr.hasNext()) {
            sb.append(elementColor);
            sb.append(itr.next());
            if (itr.hasNext()) {
                sb.append(delimiterColor);
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

}
