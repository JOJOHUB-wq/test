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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PullingEnchant extends CustomEnchant {

    private final Map<UUID, Integer> pullingArrows = new HashMap<>();

    public PullingEnchant(AtheriumEnchants plugin) {
        super("pulling", plugin);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) {
            return;
        }

        ItemStack bow = event.getBow();
        if (bow == null) return;

        int level = getLevelFromItem(bow);
        if (level > 0) {
            pullingArrows.put(event.getProjectile().getUniqueId(), level);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByArrow(EntityDamageByEntityEvent event) {
        if (!isEnabled() || !(event.getDamager() instanceof Arrow) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();
        if (!pullingArrows.containsKey(arrow.getUniqueId()) || !(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) arrow.getShooter();
        LivingEntity victim = (LivingEntity) event.getEntity();
        int level = pullingArrows.get(arrow.getUniqueId());

        double power = getConfigValue(level, "power", 0.5);

        Vector vector = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
        victim.setVelocity(vector.multiply(power));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            pullingArrows.remove(event.getEntity().getUniqueId());
        }
    }
}