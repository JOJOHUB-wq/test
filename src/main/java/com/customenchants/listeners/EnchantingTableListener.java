package com.customenchants.listeners;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnchantingTableListener implements Listener {

    private final AtheriumEnchants plugin;
    private final Random random = new Random();

    public EnchantingTableListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int enchantingLevel = event.getExpLevelCost();

        for (CustomEnchant customEnchant : plugin.getEnchantmentManager().getRegisteredEnchants()) {
            if (!customEnchant.isEnabled() || !customEnchant.isEnchantingTableEnabled() || !customEnchant.canEnchantItem(item)) {
                continue;
            }

            // Check for conflicts with existing and newly added vanilla enchants
            boolean conflict = false;
            List<String> conflicts = plugin.getEnchantmentConfig().getConfig().getStringList("enchantments." + customEnchant.getName() + ".conflicts_with");
            for (Map.Entry<Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                if (conflicts.contains(entry.getKey().getKey().getKey().toUpperCase())) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) continue;

            // Calculate final chance
            double baseChance = customEnchant.getEnchantingChance();
            // The higher the enchanting level, the higher the chance
            double finalChance = baseChance * (1 + (enchantingLevel / 30.0));

            if (random.nextDouble() < finalChance) {
                // Determine the level of the custom enchant
                // For simplicity, we'll grant a random level up to the max.
                // A more complex system could weigh lower levels more heavily.
                int enchantLevel = random.nextInt(customEnchant.getMaxLevel()) + 1;
                customEnchant.applyToItem(item, enchantLevel);
            }
        }
    }
}