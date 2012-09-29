package mccity.plugins.pvprealm.tasks;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.GUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountdownTask implements Runnable {

    private static final Map<String, Long> playersLastUseTime = new HashMap<String, Long>();
    private static final int USE_COOLDOWN = 10000;
    private static final int PERIOD = 30;
    private static final int RADIUS = 50;

    private static final String[] cdStrings = {
            "&a$1: &e3..",
            "&a$1: &e2..",
            "&a$1: &e1..",
            "&a$1: &cGo!"
    };

    private final PvpRealm plugin;
    private final PvpPlayer pvpPlayer;
    private int taskId;

    private final List<Player> notifyList = new ArrayList<Player>();
    private int stringIndex = 0;

    public CountdownTask(PvpRealm plugin, PvpPlayer pvpPlayer) {
        this.plugin = plugin;
        this.pvpPlayer = pvpPlayer;
    }

    @Override
    public void run() {
        if (stringIndex < cdStrings.length) {
            for (Player notifyPlayer : notifyList) {
                GUtils.sendMessage(notifyPlayer, cdStrings[stringIndex], pvpPlayer.getName());
            }
            stringIndex++;
        } else {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void start() {
        Long lastUseTime = playersLastUseTime.get(pvpPlayer.getName());
        if (lastUseTime == null || System.currentTimeMillis() > lastUseTime + USE_COOLDOWN) {
            notifyList.add(pvpPlayer.getPlayer());
            List<Entity> nearbyEntities = pvpPlayer.getPlayer().getNearbyEntities(RADIUS, RADIUS, RADIUS);
            for (Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity instanceof Player) {
                    notifyList.add((Player) nearbyEntity);
                }
            }
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, PERIOD, PERIOD);
            playersLastUseTime.put(pvpPlayer.getName(), System.currentTimeMillis());
        } else {
            GUtils.sendTranslated(pvpPlayer.getPlayer(), "countdown.cooldown");
        }
    }
}
