package com.customenchants.enchants;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HunterEnchant extends CustomEnchant {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public String getName() {
        return "Hunter";
    }

    @Override
    public int getMaxLevel() {
        return 3; // Epic enchant
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.BOW;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (bow.getType() != Material.BOW) {
            return;
        }

        int level = getLevelFromItem(bow);
        if (level <= 0) {
            return;
        }

        // Cooldown: 20 ticks (1s) base, reduced by 4 ticks per level
        long cooldownTime = 20 - (level * 4L);
        long lastShot = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastShot < cooldownTime * 50) { // Convert ticks to ms
            return;
        }

        // Check for arrows
        if (!player.getInventory().contains(Material.ARROW) && player.getGameMode() != GameMode.CREATIVE) {
            return;
        }

        event.setCancelled(true); // Prevent normal bow drawing

        // Consume arrow
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
        }

        // Fire arrow
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setShooter(player);
        arrow.setPickupStatus(Arrow.PickupStatus.ALLOWED);
        // Force of 3.0 is equivalent to a fully drawn bow
        arrow.setVelocity(player.getLocation().getDirection().multiply(3.0));

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}