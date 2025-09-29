package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HawkEnchant extends CustomEnchant {

    private final Map<UUID, Integer> hawkArrows = new HashMap<>();

    public HawkEnchant(AtheriumEnchants plugin) {
        super("Hawk", plugin);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!isEnabled()) return;

        if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        if (bow == null) return;

        int level = getLevelFromItem(bow);
        if (level <= 0) {
            return;
        }

        if (!player.hasGravity() || !player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            Arrow arrow = (Arrow) event.getProjectile();
            hawkArrows.put(arrow.getUniqueId(), level);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByArrow(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

        if (!(event.getDamager() instanceof Arrow) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        UUID arrowId = arrow.getUniqueId();

        if (!hawkArrows.containsKey(arrowId)) {
            return;
        }

        int level = hawkArrows.get(arrowId);

        double damageMultiplier = getConfigValue(level, "damage_multiplier", 1.15);
        double bonusDamage = event.getDamage() * (damageMultiplier - 1.0);

        event.setDamage(event.getDamage() + bonusDamage);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            UUID arrowId = event.getEntity().getUniqueId();
            hawkArrows.remove(arrowId);
        }
    }
}