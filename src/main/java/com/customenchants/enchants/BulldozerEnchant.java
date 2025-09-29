package com.customenchants.enchants;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BulldozerEnchant extends CustomEnchant {

    private final Set<UUID> activeMiners = new HashSet<>();

    @Override
    public String getName() {
        return "Bulldozer";
    }

    @Override
    public int getMaxLevel() {
        return 1; // Legendary, so one level is sufficient
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        String typeName = item.getType().name();
        return typeName.endsWith("_PICKAXE") || typeName.endsWith("_SHOVEL") || typeName.endsWith("_AXE");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (activeMiners.contains(player.getUniqueId())) {
            return; // Prevent recursion
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = getLevelFromItem(tool);
        if (level <= 0 || player.isSneaking()) {
            return;
        }

        activeMiners.add(player.getUniqueId());

        try {
            List<Block> blocksToBreak = getNearbyBlocks(event.getBlock(), player);
            for (Block block : blocksToBreak) {
                if (tool.getItemMeta() instanceof Damageable) {
                    Damageable damageable = (Damageable) tool.getItemMeta();
                    if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                        break; // Stop if tool breaks
                    }
                }

                BlockBreakEvent newEvent = new BlockBreakEvent(block, player);
                Bukkit.getPluginManager().callEvent(newEvent);

                if (!newEvent.isCancelled()) {
                    block.breakNaturally(tool);
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        damageTool(tool);
                    }
                }
            }
        } finally {
            activeMiners.remove(player.getUniqueId());
        }
    }

    private List<Block> getNearbyBlocks(Block centerBlock, Player player) {
        List<Block> blocks = new ArrayList<>();
        BlockFace face = getPlayerBlockFace(player);
        if (face == null) return blocks;

        int radius = 1; // 3x3 area

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block relative;
                    if (face == BlockFace.UP || face == BlockFace.DOWN) {
                        relative = centerBlock.getRelative(x, 0, z);
                    } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                        relative = centerBlock.getRelative(x, y, 0);
                    } else { // EAST or WEST
                        relative = centerBlock.getRelative(0, y, z);
                    }

                    if (relative.getType() != Material.AIR && relative.isPreferredTool(player.getInventory().getItemInMainHand())) {
                         blocks.add(relative);
                    }
                }
            }
        }
        return blocks;
    }

    private BlockFace getPlayerBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 10);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) {
            return null;
        }
        return lastTwoTargetBlocks.get(1).getFace(lastTwoTargetBlocks.get(0));
    }

    private void damageTool(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(damageable.getDamage() + 1);
            tool.setItemMeta(meta);
        }
    }
}