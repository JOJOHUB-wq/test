package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class DodgeEnchant extends CustomEnchant {

    public DodgeEnchant(AtheriumEnchants plugin) {
        super("Dodge", plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;

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

        double dodgeChancePerLevel = getConfigValue(totalLevel, "dodge_chance_per_level", 0.015);
        double totalDodgeChance = totalLevel * dodgeChancePerLevel;


        if (ThreadLocalRandom.current().nextDouble() < totalDodgeChance) {
            event.setCancelled(true);
        }
    }
}