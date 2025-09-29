package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class PoisonEnchant extends CustomEnchant {

    public PoisonEnchant(AtheriumEnchants plugin) {
        super("Poison", plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

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

        double chance = getConfigValue(level, "chance", 0.15);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            // Duration in seconds from config, converted to ticks.
            int duration = getConfigValue(level, "duration", 3) * 20;
            // Amplifier from config (0 for Poison I, 1 for Poison II).
            int amplifier = getConfigValue(level, "amplifier", level - 1);

            victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
        }
    }
}