package com.customenchants.enchants;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PoisonThornsEnchant extends CustomEnchant {

    private static final int MAX_DURATION_TICKS = 200; // 10 seconds max

    @Override
    public String getName() {
        return "Poison-Thorns";
    }

    @Override
    public int getMaxLevel() {
        return 2; // Epic enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_BOOTS");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof LivingEntity)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getDamager();

        if (victim.getEquipment() == null || victim.getEquipment().getBoots() == null) {
            return;
        }

        ItemStack boots = victim.getEquipment().getBoots();
        int level = getLevelFromItem(boots);
        if (level <= 0) {
            return;
        }

        // Duration calculation: 1 second (20 ticks) per heart (2 damage) of damage dealt, per level.
        double damage = event.getFinalDamage();
        int duration = (int) ((damage / 2.0) * (20 * level));

        if (duration > MAX_DURATION_TICKS) {
            duration = MAX_DURATION_TICKS;
        }

        if (duration <= 0) return;

        // Amplifier: 0 for level 1 (Poison I), 1 for level 2 (Poison II)
        int amplifier = level - 1;

        attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
    }
}