package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class LavawalkerEnchant extends CustomEnchant {

    private final Map<Block, Long> cooledBlocks = new HashMap<>();

    public LavawalkerEnchant(AtheriumEnchants plugin) {
        super("lavawalker", plugin);
        startCleanupTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getEquipment() == null || player.getEquipment().getBoots() == null) {
            return;
        }

        int level = getLevelFromItem(player.getEquipment().getBoots());
        if (level <= 0) {
            return;
        }

        Location loc = player.getLocation();
        int radius = getConfigValue(level, "radius", 2);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = loc.clone().add(x, -1, z).getBlock();
                if (block.getType() == Material.LAVA) {
                    Block solidBlock = block.getRelative(BlockFace.UP);
                    if (solidBlock.getType().isAir()) {
                        block.setType(Material.OBSIDIAN);
                        cooledBlocks.put(block, System.currentTimeMillis());
                    }
                }
            }
        }
    }

    private void startCleanupTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                cooledBlocks.entrySet().removeIf(entry -> {
                    if (now - entry.getValue() > 3000) { // 3 seconds
                        if (entry.getKey().getType() == Material.OBSIDIAN) {
                            entry.getKey().setType(Material.LAVA);
                        }
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
}