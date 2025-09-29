package com.customenchants.enchants;

import org.bukkit.Material;
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

    @Override
    public String getName() {
        return "Hawk";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Epic enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
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

        // Check if player is in the air
        if (!player.hasGravity() || !player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) {
            Arrow arrow = (Arrow) event.getProjectile();
            hawkArrows.put(arrow.getUniqueId(), level);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByArrow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        UUID arrowId = arrow.getUniqueId();

        if (!hawkArrows.containsKey(arrowId)) {
            return;
        }

        int level = hawkArrows.get(arrowId);

        // Bonus damage: 15% per level
        double bonusDamage = event.getDamage() * (level * 0.15);

        event.setDamage(event.getDamage() + bonusDamage);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Clean up the map when the arrow lands to prevent memory leaks
        if (event.getEntity() instanceof Arrow) {
            UUID arrowId = event.getEntity().getUniqueId();
            hawkArrows.remove(arrowId);
        }
    }
}