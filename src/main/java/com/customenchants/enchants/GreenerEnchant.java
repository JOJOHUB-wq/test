package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GreenerEnchant extends CustomEnchant {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GreenerEnchant(AtheriumEnchants plugin) {
        super("greener", plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = getLevelFromItem(tool);
        if (level <= 0) {
            return;
        }

        long cooldownTime = getConfigValue(level, "cooldown_seconds", 10) * 1000L;
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + cooldownTime) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Озеленитель на перезарядке еще " + (timeLeft / 1000) + " сек.");
                return;
            }
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        int radius = getConfigValue(level, "radius", 2);
        boolean success = false;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = clickedBlock.getLocation().add(x, y, z).getBlock();
                    if (block.getType() == Material.DIRT) {
                        block.setType(Material.GRASS_BLOCK);
                        success = true;
                    } else if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE) {
                        block.setType(Material.MOSS_BLOCK);
                        success = true;
                    }
                }
            }
        }

        if (success) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            player.swingMainHand();
        }
    }
}