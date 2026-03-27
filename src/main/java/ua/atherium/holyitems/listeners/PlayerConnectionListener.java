package ua.atherium.holyitems.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.atherium.holyitems.HolyWorldItems;

public class PlayerConnectionListener implements Listener {

    private final HolyWorldItems plugin;

    public PlayerConnectionListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().loadData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().saveData(event.getPlayer());
    }
}
