package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PingerEnchant extends CustomEnchant {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PingerEnchant(AtheriumEnchants plugin) {
        super("pinger", plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = getLevelFromItem(tool);
        if (level <= 0) {
            return;
        }

        long cooldownTime = getConfigValue(level, "cooldown_seconds", 30) * 1000L;
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) + cooldownTime) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Пингер на перезарядке еще " + (timeLeft / 1000) + " сек.");
                return;
            }
        }

        int radius = getConfigValue(level, "radius", 10);
        int duration = getConfigValue(level, "duration_seconds", 5) * 20;

        Set<Block> ores = findOres(player.getLocation(), radius);
        if (ores.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "Руды поблизости не найдены.");
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        for (Block ore : ores) {
            BlockDisplay glow = player.getWorld().spawn(ore.getLocation(), BlockDisplay.class, (e) -> {
                e.setBlock(ore.getBlockData());
                e.setGlowing(true);
                e.setBrightness(new BlockDisplay.Brightness(15, 15));
            });

            plugin.getServer().getScheduler().runTaskLater(plugin, glow::remove, duration);
        }
    }

    private Set<Block> findOres(Location center, int radius) {
        Set<Block> ores = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getType().name().contains("_ORE")) {
                        ores.add(block);
                    }
                }
            }
        }
        return ores;
    }
}