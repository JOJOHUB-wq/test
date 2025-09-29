package com.customenchants.enchants;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class PoisonEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Poison";
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_SWORD");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
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

        // Chance to poison: 15% per level
        double chance = level * 0.15;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            // Duration in seconds: 3 seconds per level. PotionEffect takes ticks (20 ticks/sec)
            int duration = level * 3 * 20;
            // Amplifier: 0 for Poison I, 1 for Poison II. Level 1 -> Poison I, Level 2 -> Poison II
            int amplifier = level - 1;

            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
        }
    }
}