package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class OxidationEnchant extends CustomEnchant {

    public OxidationEnchant(AtheriumEnchants plugin) {
        super("Oxidation", plugin);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

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

        int extraDamage = getConfigValue(level, "extra_damage", 1);

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