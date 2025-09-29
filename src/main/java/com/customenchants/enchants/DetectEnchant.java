package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class DetectEnchant extends CustomEnchant implements Listener {

    public DetectEnchant(AtheriumEnchants plugin) {
        super("detect", plugin);
        startEffectTask();
    }

    private void startEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled()) return;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getEquipment() == null || player.getEquipment().getHelmet() == null) {
                        continue;
                    }
                    int level = getLevelFromItem(player.getEquipment().getHelmet());
                    if (level > 0) {
                        int radius = getConfigValue(level, "radius", 20);
                        int duration = getConfigValue(level, "duration_seconds", 3) * 20;

                        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                            if (entity instanceof Player && !entity.equals(player)) {
                                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, true, false));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Check every 5 seconds
    }
}