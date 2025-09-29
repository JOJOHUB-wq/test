package com.customenchants.enchants;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ExperiencedEnchant extends CustomEnchant {

    private static final List<Material> APPLICABLE_MATERIALS = Arrays.asList(
            Material.NETHERITE_SWORD, Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
            Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE,
            Material.NETHERITE_AXE, Material.DIAMOND_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE,
            Material.NETHERITE_SHOVEL, Material.DIAMOND_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL,
            Material.BOW, Material.CROSSBOW, Material.TRIDENT,
            Material.NETHERITE_HOE, Material.DIAMOND_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.STONE_HOE, Material.WOODEN_HOE
    );

    @Override
    public String getName() {
        return "Experienced";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Uncommon enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return APPLICABLE_MATERIALS.contains(item.getType());
    }

    // Handle XP from killing mobs
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
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
        // Increase XP by 20% per level
        int newExp = (int) (originalExp * (1 + (level * 0.20)));
        event.setDroppedExp(newExp);
    }

    // Handle XP from breaking blocks
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
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

        // Increase XP by 20% per level
        int newExp = (int) (expToDrop * (1 + (level * 0.20)));
        event.setExpToDrop(newExp);
    }
}