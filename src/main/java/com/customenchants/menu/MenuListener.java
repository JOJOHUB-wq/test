package com.customenchants.menu;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuListener implements Listener {

    private final AtheriumEnchants plugin;

    public MenuListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof Menu)) {
            return;
        }

        event.setCancelled(true);

        Menu menu = (Menu) holder;
        String itemKey = menu.getItemKey(event.getSlot());

        if (itemKey == null) {
            return;
        }

        FileConfiguration menuConfig = plugin.getMenuManager().getMenuConfig(menu.getId());
        if (menuConfig == null) {
            return;
        }

        String clickTypePath = event.isLeftClick() ? "on_left_click" : "on_right_click";
        List<String> commands = menuConfig.getStringList("items." + itemKey + "." + clickTypePath + ".commands");

        if (!commands.isEmpty()) {
            for (String command : commands) {
                processCommand(player, command);
            }
        } else {
            // If no commands, try to handle it as a purchase
            handlePurchase(player, itemKey, menuConfig);
        }
    }

    private void handlePurchase(Player player, String itemKey, FileConfiguration menuConfig) {
        ConfigurationSection itemSection = menuConfig.getConfigurationSection("items." + itemKey);
        if (itemSection == null || itemSection.getString("material") == null || !itemSection.getString("material").equals("ENCHANTED_BOOK")) {
            return; // Not a purchasable enchant book
        }

        String displayName = ChatColor.stripColor(itemSection.getString("display_name"));
        CustomEnchant enchant = plugin.getEnchantmentManager().getEnchantByDisplayName(displayName);

        if (enchant == null) {
            return;
        }

        double price = plugin.getEnchantmentConfig().getConfig().getDouble("enchantments." + enchant.getName() + ".price", 0);

        if (price <= 0) {
            player.sendMessage(ChatColor.RED + "This item cannot be purchased.");
            return;
        }

        if (!plugin.getEconomyManager().hasEnough(player, price)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money to buy this. Price: " + price);
            return;
        }

        if (plugin.getEconomyManager().withdraw(player, price)) {
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            enchant.applyToItem(book, 1); // Give level 1 book by default
            player.getInventory().addItem(book);
            player.sendMessage(ChatColor.GREEN + "You have purchased " + enchant.getDisplayName() + " for " + price + "!");
        } else {
            player.sendMessage(ChatColor.RED + "An error occurred during the transaction.");
        }
    }

    private void processCommand(Player player, String command) {
        if (command.startsWith("[OPEN]")) {
            String menuId = command.substring(7).trim();
            plugin.getMenuManager().openMenu(player, menuId);
        } else if (command.equalsIgnoreCase("[CLOSE]")) {
            player.closeInventory();
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }
}