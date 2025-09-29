package com.customenchants.enchants;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class OxidationEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Oxidation";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Legendary enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_SWORD");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
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

        if (victim.getEquipment() == null) {
            return;
        }

        // Apply extra damage to victim's armor
        // 1 extra durability point per level
        int extraDamage = level;

        for (ItemStack armorPiece : victim.getEquipment().getArmorContents()) {
            if (armorPiece != null && armorPiece.getItemMeta() instanceof Damageable) {
                ItemMeta meta = armorPiece.getItemMeta();
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(damageable.getDamage() + extraDamage);
                armorPiece.setItemMeta(meta);
            }
        }
    }
}