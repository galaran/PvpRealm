package me.galaran.bukkitutils.pvprealm;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

public class Tools {

    public static void printEventHandlers(Event event, Player player) {
        for (RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
            player.sendMessage(registeredListener.getPlugin().getName() + " " + registeredListener.getListener().getClass().getSimpleName() +
                    " " + registeredListener.getPriority().name());
        }
    }
}
