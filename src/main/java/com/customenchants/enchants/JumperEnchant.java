package com.customenchants.enchants;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

public class JumperEnchant extends CustomEnchant {

    @Override
    public String getName() {
        return "Jumper";
    }

    @Override
    public int getMaxLevel() {
        return 1; // Epic enchant, one level is enough
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType().name().endsWith("_BOOTS");
    }

    // Allow flight when player is on the ground and wearing the boots
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (player.getEquipment() != null && player.getEquipment().getBoots() != null) {
            int level = getLevelFromItem(player.getEquipment().getBoots());
            if (level > 0 && (!player.getAllowFlight() && player.hasGravity() && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                player.setAllowFlight(true);
            }
        }
    }

    // Handle the double jump
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (player.getEquipment() == null || player.getEquipment().getBoots() == null) {
            return;
        }

        int level = getLevelFromItem(player.getEquipment().getBoots());
        if (level > 0) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);

            // Apply the jump velocity
            player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1));
        }
    }

    // Initial check on join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
         if (player.getEquipment() != null && player.getEquipment().getBoots() != null) {
            int level = getLevelFromItem(player.getEquipment().getBoots());
            if (level > 0 && (player.hasGravity() && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                 player.setAllowFlight(true);
            }
        }
    }
}