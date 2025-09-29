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

public class GlowEnchant extends CustomEnchant {

    public GlowEnchant(AtheriumEnchants plugin) {
        super("Glow", plugin);
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

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        double chance = getConfigValue(level, "chance", 0.5);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            int duration = getConfigValue(level, "duration", 10) * 20;
            int amplifier = getConfigValue(level, "amplifier", 0);

            victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, amplifier));
        }
    }
}