package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
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
            PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.NAUSEA
    );

    public StuporEnchant(AtheriumEnchants plugin) {
        super("Stupor", plugin);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

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

        double chance = getConfigValue(level, "chance", 0.15);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            PotionEffectType randomEffect = NEGATIVE_EFFECTS.get(ThreadLocalRandom.current().nextInt(NEGATIVE_EFFECTS.size()));

            int duration = getConfigValue(level, "duration", 3) * 20;
            int amplifier = getConfigValue(level, "amplifier", level - 1);

            victim.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));
        }
    }
}