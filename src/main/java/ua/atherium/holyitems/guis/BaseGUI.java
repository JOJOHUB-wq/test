package ua.atherium.holyitems.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ua.atherium.holyitems.HolyWorldItems;

public abstract class BaseGUI implements InventoryHolder {

    protected final HolyWorldItems plugin;
    protected final Inventory inventory;
    protected final Player player;

    public BaseGUI(HolyWorldItems plugin, Player player, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public abstract void handleClick(InventoryClickEvent event);
}
