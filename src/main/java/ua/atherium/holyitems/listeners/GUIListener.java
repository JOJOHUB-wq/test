package ua.atherium.holyitems.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.guis.BaseGUI;

public class GUIListener implements Listener {

    private final HolyWorldItems plugin;

    public GUIListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            event.setCancelled(true);
            ((BaseGUI) holder).handleClick(event);
        }
    }
}
