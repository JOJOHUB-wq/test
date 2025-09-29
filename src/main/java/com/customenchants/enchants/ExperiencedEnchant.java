package com.customenchants.enchants;

import com.customenchants.CustomEnchants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ExperiencedEnchant extends CustomEnchant {

    public ExperiencedEnchant(CustomEnchants plugin) {
        super("Experienced", plugin);
    }

    // Handle XP from killing mobs
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) return;

        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        int level = getLevelFromItem(weapon);
        if (level <= 0) {
            return;
        }

        int originalExp = event.getDroppedExp();
        // Increase XP by a multiplier from the config
        double multiplier = getConfigValue(level, "xp_multiplier", 1.20);
        int newExp = (int) (originalExp * multiplier);
        event.setDroppedExp(newExp);
    }

    // Handle XP from breaking blocks
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        int expToDrop = event.getExpToDrop();
        if (expToDrop <= 0) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        int level = getLevelFromItem(tool);
        if (level <= 0) {
            return;
        }

        double multiplier = getConfigValue(level, "xp_multiplier", 1.20);
        int newExp = (int) (expToDrop * multiplier);
        event.setExpToDrop(newExp);
    }
}