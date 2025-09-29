package com.customenchants.listeners;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class AnvilListener implements Listener {

    private final AtheriumEnchants plugin;

    public AnvilListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null || secondItem.getType() != Material.ENCHANTED_BOOK) {
            return;
        }

        CustomEnchant customEnchant = null;
        int enchantLevel = 0;

        for (CustomEnchant ce : plugin.getEnchantmentManager().getRegisteredEnchants()) {
            int level = ce.getLevelFromItem(secondItem);
            if (level > 0) {
                customEnchant = ce;
                enchantLevel = level;
                break;
            }
        }

        if (customEnchant == null || !customEnchant.isEnabled()) {
            return;
        }

        if (!customEnchant.canEnchantItem(firstItem)) {
            return;
        }

        // Check for conflicts
        List<String> conflicts = plugin.getEnchantmentConfig().getConfig().getStringList("enchantments." + customEnchant.getName() + ".conflicts_with");
        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            if (conflicts.contains(entry.getKey().getKey().getKey().toUpperCase())) {
                return; // Conflict with vanilla enchant
            }
        }
        for (CustomEnchant existingEnchant : plugin.getEnchantmentManager().getRegisteredEnchants()) {
            if (existingEnchant.getLevelFromItem(firstItem) > 0 && conflicts.contains(existingEnchant.getName().toUpperCase())) {
                return; // Conflict with another custom enchant
            }
        }

        ItemStack result = firstItem.clone();
        customEnchant.applyToItem(result, enchantLevel);

        int cost = 5 * enchantLevel; // Example cost
        inventory.setRepairCost(cost);
        event.setResult(result);
    }
}