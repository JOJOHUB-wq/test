package com.customenchants.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class Menu implements InventoryHolder {

    private final String id;
    private final Inventory inventory;
    private final Map<Integer, String> slotMappings = new HashMap<>();

    public Menu(String id, String title, int size) {
        this.id = id;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void setSlotMapping(int slot, String itemKey) {
        slotMappings.put(slot, itemKey);
    }

    public String getItemKey(int slot) {
        return slotMappings.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getId() {
        return id;
    }
}