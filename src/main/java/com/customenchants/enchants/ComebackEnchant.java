package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ComebackEnchant extends CustomEnchant {

    public ComebackEnchant(AtheriumEnchants plugin) {
        super("comeback", plugin);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isEnabled() || !(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Trident trident = (Trident) event.getEntity();
        Player player = (Player) event.getEntity().getShooter();
        ItemStack tridentItem = player.getInventory().getItemInMainHand();

        int level = getLevelFromItem(tridentItem);
        if (level <= 0) {
            return;
        }

        new BukkitRunnable() {
            private int ticksLived = 0;

            @Override
            public void run() {
                if (trident.isDead() || !trident.isValid() || ticksLived > 200) { // 10 second timeout
                    this.cancel();
                    return;
                }

                if (trident.isInBlock() || trident.doesBounce()) {
                    Vector vector = player.getLocation().toVector().subtract(trident.getLocation().toVector()).normalize();
                    double speed = getConfigValue(level, "return_speed", 1.0);
                    trident.setVelocity(vector.multiply(speed));
                }

                if (trident.getLocation().distanceSquared(player.getLocation()) < 4) {
                    player.getInventory().addItem(tridentItem);
                    trident.remove();
                    this.cancel();
                }
                ticksLived++;
            }
        }.runTaskTimer(plugin, 10L, 1L);
    }
}