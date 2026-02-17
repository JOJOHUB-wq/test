package ua.atherium.holyitems.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

public abstract class BaseGUI implements InventoryHolder {

    protected final HolyWorldItems plugin;
    protected final Player player;
    protected final Inventory inventory;

    public BaseGUI(HolyWorldItems plugin, Player player, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, ChatUtil.color(title));
    }

    public abstract void handleClick(InventoryClickEvent event);

    public void handleClose(InventoryCloseEvent event) {}

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected void fillBorder(ItemStack item) {
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, item);
            }
        }
    }
}
