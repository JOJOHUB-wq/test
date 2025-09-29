package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PoisonThornsEnchant extends CustomEnchant {

    public PoisonThornsEnchant(AtheriumEnchants plugin) {
        super("Poison-Thorns", plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

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

        double damage = event.getFinalDamage();
        double multiplier = getConfigValue(level, "damage_to_duration_multiplier", 10.0); // Ticks per heart
        int maxDuration = getConfigValue(level, "max_duration_ticks", 200);

        int duration = (int) (damage / 2.0 * multiplier);

        if (duration > maxDuration) {
            duration = maxDuration;
        }

        if (duration <= 0) return;

        int amplifier = getConfigValue(level, "amplifier", level - 1);

        attacker.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
    }
}