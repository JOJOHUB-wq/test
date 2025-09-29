package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoutEnchant extends CustomEnchant implements Listener {

    public ScoutEnchant(AtheriumEnchants plugin) {
        super("scout", plugin);
        startEffectTask();
    }

    private void startEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled()) return;
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getEquipment() == null || player.getEquipment().getBoots() == null) {
                        continue;
                    }
                    int level = getLevelFromItem(player.getEquipment().getBoots());
                    if (level > 0) {
                        int amplifier = getConfigValue(level, "amplifier", 0);
                        // Apply effect for 12 seconds, it will be refreshed every 10 seconds
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, amplifier, true, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L); // Check every 10 seconds
    }
}