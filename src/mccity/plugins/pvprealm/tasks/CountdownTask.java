package mccity.plugins.pvprealm.tasks;

import mccity.plugins.pvprealm.PvpRealm;
import mccity.plugins.pvprealm.object.PvpPlayer;
import me.galaran.bukkitutils.pvprealm.text.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
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

    private final PvpPlayer pvpPlayer;

    private Location loc;
    private int taskId;
    private int curLine = 0;

    public CountdownTask(PvpPlayer pvpPlayer) {
        this.pvpPlayer = pvpPlayer;
    }

    @Override
    public void run() {
        if (curLine < cdStrings.length) {
            Messaging.broadcastRawNoPrefix(loc, RADIUS, cdStrings[curLine], pvpPlayer.getName());
            curLine++;
        } else {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void start() {
        Long lastUseTime = playersLastUseTime.get(pvpPlayer.getName());
        if (lastUseTime == null || System.currentTimeMillis() > lastUseTime + USE_COOLDOWN) {
            playersLastUseTime.put(pvpPlayer.getName(), System.currentTimeMillis());
            loc = pvpPlayer.getPlayer().getLocation();
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(PvpRealm.getSelf(), this, PERIOD, PERIOD);
        } else {
            Messaging.send(pvpPlayer.getPlayer(), "signs.countdown.cooldown");
        }
    }
}
