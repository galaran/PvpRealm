package me.galaran.bukkitutils.pvprealm;

import org.bukkit.ChatColor;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final Pattern AMP_COLOR_PATTERN = Pattern.compile("&([0-9A-FK-OR])", Pattern.CASE_INSENSITIVE);

    /**
     * @param toStringer or null to convert with toString()
     */
    public static <T> String join(Iterable<T> collection, String delimiter, ToStringer<T> toStringer) {
        return join(collection, null, delimiter, null, toStringer);
    }

    /**
     * @param toStringer or null to convert with toString()
     */
    public static <T> String join(Iterable<T> collection, ChatColor elementColor, String delimiter, ChatColor delimiterColor, ToStringer<T> toStringer) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> itr = collection.iterator();
        while (itr.hasNext()) {
            T obj = itr.next();
            String objString = (toStringer == null) ? obj.toString() : toStringer.toString(obj);

            if (elementColor != null) {
                sb.append(elementColor);
            }
            sb.append(objString);
            if (itr.hasNext()) {
                if (delimiterColor != null) {
                    sb.append(delimiterColor);
                }
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    public static String colorizeAmps(String string) {
        Matcher m = AMP_COLOR_PATTERN.matcher(string);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            m.appendReplacement(sb, ChatColor.COLOR_CHAR + m.group(1));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static String parameterizeString(String pattern, Object... params) {
        String result = pattern;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                result = result.replace("$" + (i + 1), params[i].toString());
            }
        }
        return result;
    }
}
