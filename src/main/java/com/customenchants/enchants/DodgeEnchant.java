package com.customenchants.enchants;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class DodgeEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Dodge";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Epic enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        String typeName = item.getType().name();
        return typeName.endsWith("_HELMET") || typeName.endsWith("_CHESTPLATE") || typeName.endsWith("_LEGGINGS") || typeName.endsWith("_BOOTS");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (player.getEquipment() == null) {
            return;
        }

        int totalLevel = 0;
        for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
            if (armorPiece != null) {
                totalLevel += getLevelFromItem(armorPiece);
            }
        }

        if (totalLevel <= 0) {
            return;
        }

        // Dodge chance: 1.5% per total level.
        // E.g., a full set of level 3 (total level 12) gives an 18% dodge chance.
        double dodgeChance = totalLevel * 0.015;

        if (ThreadLocalRandom.current().nextDouble() < dodgeChance) {
            event.setCancelled(true);
            // Optionally, add a sound effect or particle to indicate a successful dodge.
        }
    }
}