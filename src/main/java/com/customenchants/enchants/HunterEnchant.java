package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
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

    public HunterEnchant(AtheriumEnchants plugin) {
        super("Hunter", plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;

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

        long cooldownTime = getConfigValue(level, "cooldown_ticks", 20) * 50L; // Convert ticks to ms
        long lastShot = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastShot < cooldownTime) {
            return;
        }

        if (!player.getInventory().contains(Material.ARROW) && player.getGameMode() != GameMode.CREATIVE) {
            return;
        }

        event.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
        }

        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setShooter(player);
        arrow.setPickupStatus(Arrow.PickupStatus.ALLOWED);

        double power = getConfigValue(level, "power", 3.0);
        arrow.setVelocity(player.getLocation().getDirection().multiply(power));

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}