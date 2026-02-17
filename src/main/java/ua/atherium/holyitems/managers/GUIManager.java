package ua.atherium.holyitems.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.guis.BaseGUI;

public class GUIManager implements Listener {

    private final HolyWorldItems plugin;

    public GUIManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            event.setCancelled(true);
            ((BaseGUI) holder).handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            ((BaseGUI) holder).handleClose(event);
        }
    }
}
