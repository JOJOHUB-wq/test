package com.customenchants.enchants;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class VampirismEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Vampirism";
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_SWORD");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        // Chance to heal: 10% per level
        double chance = level * 0.10;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            // Heal amount: 0.5 hearts per level
            double healAmount = level * 1.0;

            if (currentHealth + healAmount > maxHealth) {
                player.setHealth(maxHealth);
            } else {
                player.setHealth(currentHealth + healAmount);
            }
        }
    }
}