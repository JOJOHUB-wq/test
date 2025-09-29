package com.customenchants.enchants;

import org.bukkit.Location;
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

public class SniperEnchant extends CustomEnchant {

    private final Map<UUID, Integer> sniperArrowsLevel = new HashMap<>();
    private final Map<UUID, Location> sniperArrowsLocation = new HashMap<>();

    @Override
    public String getName() {
        return "Sniper";
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

        Arrow arrow = (Arrow) event.getProjectile();
        sniperArrowsLevel.put(arrow.getUniqueId(), level);
        sniperArrowsLocation.put(arrow.getUniqueId(), arrow.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByArrow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        UUID arrowId = arrow.getUniqueId();

        if (!sniperArrowsLevel.containsKey(arrowId)) {
            return;
        }

        int level = sniperArrowsLevel.get(arrowId);
        Location startLocation = sniperArrowsLocation.get(arrowId);
        Location endLocation = arrow.getLocation();

        double distance = startLocation.distance(endLocation);

        // Bonus damage: 0.05 hearts (0.1 damage) per block per level
        double bonusDamage = distance * level * 0.1;

        event.setDamage(event.getDamage() + bonusDamage);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Clean up the maps when the arrow lands to prevent memory leaks
        if (event.getEntity() instanceof Arrow) {
            UUID arrowId = event.getEntity().getUniqueId();
            sniperArrowsLevel.remove(arrowId);
            sniperArrowsLocation.remove(arrowId);
        }
    }
}