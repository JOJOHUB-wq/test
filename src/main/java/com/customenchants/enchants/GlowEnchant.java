package com.customenchants.enchants;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class GlowEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Glow";
    }

    @Override
    public int getMaxLevel() {
        return 1; // Legendary, one level is enough for this effect
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_SWORD");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack weapon = damager.getInventory().getItemInMainHand();

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        // Chance to apply glow: 50%
        if (ThreadLocalRandom.current().nextDouble() < 0.50) {
            // Duration: 10 seconds. PotionEffect takes ticks (20 ticks/sec).
            int duration = 10 * 20;
            int amplifier = 0; // Standard glow effect

            victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, amplifier));
        }
    }
}