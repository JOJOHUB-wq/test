package com.customenchants.enchants;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AutoSmeltEnchant extends CustomEnchant {

    private final Map<Material, Material> smeltables = new HashMap<>();

    public AutoSmeltEnchant() {
        // Ores
        smeltables.put(Material.IRON_ORE, Material.IRON_INGOT);
        smeltables.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        smeltables.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        smeltables.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        smeltables.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        smeltables.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        smeltables.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        // Stone
        smeltables.put(Material.COBBLESTONE, Material.STONE);
        smeltables.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE_TILES); // Smelted deepslate
        // Sand
        smeltables.put(Material.SAND, Material.GLASS);
        smeltables.put(Material.RED_SAND, Material.GLASS);
        // Clay
        smeltables.put(Material.CLAY, Material.TERRACOTTA);
        // Other
        smeltables.put(Material.NETHERRACK, Material.NETHER_BRICK);
        smeltables.put(Material.WET_SPONGE, Material.SPONGE);
        smeltables.put(Material.CACTUS, Material.GREEN_DYE);

        // Wood -> Charcoal
        for (Material material : Material.values()) {
            if (material.name().endsWith("_LOG")) {
                smeltables.put(material, Material.CHARCOAL);
            }
        }
    }

    @Override
    public String getName() {
        return "AutoSmelt";
    }

    @Override
    public int getMaxLevel() {
        return 1; // Legendary, one level is enough
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        String typeName = item.getType().name();
        return typeName.endsWith("_PICKAXE") || typeName.endsWith("_SHOVEL") || typeName.endsWith("_AXE");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();

        int level = getLevelFromItem(tool);
        if (level <= 0 || player.isSneaking() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Material dropType = smeltables.get(block.getType());
        if (dropType == null) {
            return;
        }

        // Handle fortune
        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        int amountToDrop = 1;
        if (fortuneLevel > 0) {
            // A simple fortune implementation
            if (ThreadLocalRandom.current().nextInt(100) < (fortuneLevel * 25)) {
                 amountToDrop += ThreadLocalRandom.current().nextInt(fortuneLevel);
            }
        }

        // Cancel original drops and drop the smelted item
        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(dropType, amountToDrop));

        // Drop experience for smelting
        event.setExpToDrop(event.getExpToDrop() + 1);
    }
}