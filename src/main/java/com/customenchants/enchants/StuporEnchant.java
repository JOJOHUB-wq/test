package com.customenchants.enchants;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StuporEnchant extends CustomEnchant {

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = Arrays.asList(
            PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS,
            PotionEffectType.CONFUSION
    );

    @Override
    public String getName() {
        return "Stupor";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Epic enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.TRIDENT;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // This handles melee attacks with a trident
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack weapon = damager.getInventory().getItemInMainHand();

        if (weapon.getType() != Material.TRIDENT) {
            return;
        }

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        // Chance to apply effect: 15% per level
        double chance = level * 0.15;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            PotionEffectType randomEffect = NEGATIVE_EFFECTS.get(ThreadLocalRandom.current().nextInt(NEGATIVE_EFFECTS.size()));

            // Duration: 3 seconds per level. PotionEffect takes ticks (20 ticks/sec).
            int duration = level * 3 * 20;
            // Amplifier: 0 for level 1, 1 for level 2, etc.
            int amplifier = level - 1;

            victim.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));
        }
    }
}