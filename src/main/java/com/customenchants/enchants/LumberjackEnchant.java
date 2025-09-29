package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class LumberjackEnchant extends CustomEnchant {

    private final Set<UUID> activeLumberjacks = new HashSet<>();

    public LumberjackEnchant(AtheriumEnchants plugin) {
        super("Lumberjack", plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (activeLumberjacks.contains(player.getUniqueId())) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!tool.getType().name().endsWith("_AXE")) {
            return;
        }

        int level = getLevelFromItem(tool);
        if (level <= 0 || player.isSneaking() || !isLog(event.getBlock().getType())) {
            return;
        }

        activeLumberjacks.add(player.getUniqueId());

        try {
            int maxBlocks = getConfigValue(level, "max_blocks", 40);
            Set<Block> treeBlocks = findTree(event.getBlock(), maxBlocks);
            int blocksBroken = 0;
            for (Block block : treeBlocks) {
                if (blocksBroken >= maxBlocks) {
                    break;
                }

                if (tool.getItemMeta() instanceof Damageable) {
                    Damageable damageable = (Damageable) tool.getItemMeta();
                    if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                        break;
                    }
                }

                block.breakNaturally(tool);
                if (player.getGameMode() != GameMode.CREATIVE) {
                    damageTool(tool);
                }
                blocksBroken++;
            }
        } finally {
            activeLumberjacks.remove(player.getUniqueId());
        }
    }

    private Set<Block> findTree(Block startBlock, int maxBlocks) {
        Set<Block> tree = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);
        Material logType = startBlock.getType();

        while (!toCheck.isEmpty() && tree.size() < maxBlocks * 2) {
            Block current = toCheck.poll();
            if (tree.contains(current) || current.getType() != logType) {
                continue;
            }
            tree.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = current.getRelative(x, y, z);
                        if (relative.getType() == logType && !tree.contains(relative)) {
                            toCheck.add(relative);
                        }
                    }
                }
            }
        }
        return tree;
    }

    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_WOOD");
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