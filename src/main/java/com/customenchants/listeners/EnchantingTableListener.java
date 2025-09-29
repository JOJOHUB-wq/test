package com.customenchants.listeners;

import com.customenchants.CustomEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnchantingTableListener implements Listener {

    private final CustomEnchants plugin;
    private final Random random = new Random();

    public EnchantingTableListener(CustomEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int level = event.getExpLevelCost();

        for (CustomEnchant customEnchant : plugin.getEnchantmentManager().getRegisteredEnchants()) {
            if (!customEnchant.isEnabled() || !customEnchant.canEnchantItem(item)) {
                continue;
            }

            // Check for conflicts
            boolean conflict = false;
            List<String> conflicts = plugin.getEnchantmentConfig().getConfig().getStringList("enchantments." + customEnchant.getName() + ".conflicts_with");
            for (Map.Entry<Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                if (conflicts.contains(entry.getKey().getKey().getKey().toUpperCase())) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) continue;

            // Calculate chance based on rarity
            double chance = getChanceByRarity(customEnchant, level);
            if (random.nextDouble() < chance) {
                int enchantLevel = random.nextInt(customEnchant.getMaxLevel()) + 1;
                customEnchant.applyToItem(item, enchantLevel);
            }
        }
    }

    private double getChanceByRarity(CustomEnchant enchant, int level) {
        String rarity = plugin.getEnchantmentConfig().getConfig().getString("enchantments." + enchant.getName() + ".rarity", "COMMON").toUpperCase();
        double baseChance;
        switch (rarity) {
            case "UNCOMMON":
                baseChance = 0.10; // 10%
                break;
            case "RARE":
                baseChance = 0.05; // 5%
                break;
            case "EPIC":
                baseChance = 0.02; // 2%
                break;
            case "LEGENDARY":
                baseChance = 0.005; // 0.5%
                break;
            default: // COMMON
                baseChance = 0.20; // 20%
                break;
        }
        // Increase chance slightly with higher enchanting levels
        return baseChance * (1 + (level / 30.0));
    }
}