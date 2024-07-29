package net.azisaba.joinfilter.listener;

import net.azisaba.joinfilter.JoinFilter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerListener implements Listener {
    private final JoinFilter plugin;

    public PlayerListener(JoinFilter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (!plugin.getConfig().getBoolean("kick-if-not-registered", false)) {
            return;
        }
        if (!plugin.isAllowed(e.getUniqueId())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, plugin.getKickMessage());
        }
    }
}
