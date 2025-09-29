package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BomberEnchant extends CustomEnchant {

    private final Map<UUID, Integer> bomberArrows = new HashMap<>();

    public BomberEnchant(AtheriumEnchants plugin) {
        super("bomber", plugin);
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
            bomberArrows.put(event.getProjectile().getUniqueId(), level);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isEnabled() || !(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        if (!bomberArrows.containsKey(arrow.getUniqueId())) {
            return;
        }

        int level = bomberArrows.get(arrow.getUniqueId());
        Location loc = arrow.getLocation();

        float power = (float) getConfigValue(level, "power", 1.0);
        boolean setFire = getConfigValue(level, "set_fire", false);
        boolean breakBlocks = getConfigValue(level, "break_blocks", false);

        loc.getWorld().createExplosion(loc, power, setFire, breakBlocks);
        arrow.remove();
        bomberArrows.remove(arrow.getUniqueId());
    }
}