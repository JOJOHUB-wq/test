package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class VampirismEnchant extends CustomEnchant {

    public VampirismEnchant(AtheriumEnchants plugin) {
        super("Vampirism", plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        double chance = getConfigValue(level, "chance", 0.1);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            double healAmount = getConfigValue(level, "heal_amount", 1.0);

            if (currentHealth + healAmount > maxHealth) {
                player.setHealth(maxHealth);
            } else {
                player.setHealth(currentHealth + healAmount);
            }
        }
    }
}