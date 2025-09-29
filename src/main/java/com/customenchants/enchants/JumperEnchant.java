package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

public class JumperEnchant extends CustomEnchant {

    public JumperEnchant(AtheriumEnchants plugin) {
        super("Jumper", plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

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

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!isEnabled()) return;

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

            double power = getConfigValue(level, "power", 1.0);
            player.setVelocity(player.getLocation().getDirection().multiply(power * 1.5).setY(power));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
         if (player.getEquipment() != null && player.getEquipment().getBoots() != null) {
            int level = getLevelFromItem(player.getEquipment().getBoots());
            if (level > 0 && (player.hasGravity() && player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid())) {
                 player.setAllowFlight(true);
            }
        }
    }
}