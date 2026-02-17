package ua.atherium.holyitems.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final HolyWorldItems plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    public void setCooldown(Player player, String key, long seconds) {
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        playerCooldowns.put(key, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean isOnCooldown(Player player, String key) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (!playerCooldowns.containsKey(key)) return false;

        if (System.currentTimeMillis() > playerCooldowns.get(key)) {
            playerCooldowns.remove(key);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(Player player, String key) {
        if (!isOnCooldown(player, key)) return 0;
        return (cooldowns.get(player.getUniqueId()).get(key) - System.currentTimeMillis()) / 1000;
    }

    public void loadFromConfig(UUID uuid, FileConfiguration config) {
        if (config.contains("cooldowns") && config.isConfigurationSection("cooldowns")) {
            Map<String, Long> playerCooldowns = new HashMap<>();
            for (String key : config.getConfigurationSection("cooldowns").getKeys(false)) {
                long end = config.getLong("cooldowns." + key);
                if (end > System.currentTimeMillis()) {
                    playerCooldowns.put(key, end);
                }
            }
            cooldowns.put(uuid, playerCooldowns);
        }
    }

    public void saveToConfig(UUID uuid, FileConfiguration config) {
        if (!cooldowns.containsKey(uuid)) return;
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        config.set("cooldowns", null); // Clear old
        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            if (entry.getValue() > System.currentTimeMillis()) {
                config.set("cooldowns." + entry.getKey(), entry.getValue());
            }
        }
    }

    public void saveCooldowns() {
        // Handled by PlayerDataManager on quit
    }
}
