package ua.atherium.holyitems.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CooldownManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, List<Material>> filters = new ConcurrentHashMap<>();
    private final Map<UUID, Material> trapMaterials = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> debuggers = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final File dataFolder;

    public CooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "player-data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void setCooldown(Player player, String key, long seconds) {
        if (player.hasPermission("holyitems.bypass-cooldown")) return;

        long endTime = System.currentTimeMillis() + (seconds * 1000);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(key, endTime);
    }

    public boolean hasCooldown(Player player, String key) {
        if (player.hasPermission("holyitems.bypass-cooldown")) return false;

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;

        Long endTime = playerCooldowns.get(key);
        if (endTime == null) return false;

        if (System.currentTimeMillis() >= endTime) {
            playerCooldowns.remove(key);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long endTime = playerCooldowns.get(key);
        if (endTime == null) return 0;

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            playerCooldowns.remove(key);
            return 0;
        }
        return (remaining / 1000) + 1;
    }

    public void setFilter(Player player, List<Material> materials) {
        filters.put(player.getUniqueId(), new ArrayList<>(materials));
        save(player);
    }

    public List<Material> getFilter(Player player) {
        return filters.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public void setTrapMaterial(Player player, Material material) {
        trapMaterials.put(player.getUniqueId(), material);
        save(player);
    }

    public Material getTrapMaterial(Player player) {
        return trapMaterials.getOrDefault(player.getUniqueId(), Material.COBWEB);
    }

    public boolean isDebug(Player player) {
        return debuggers.contains(player.getUniqueId());
    }

    public void setDebug(Player player, boolean debug) {
        if (debug) debuggers.add(player.getUniqueId());
        else debuggers.remove(player.getUniqueId());
    }

    public void load(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Load Cooldowns
        if (config.contains("cooldowns")) {
            Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();
            for (String key : config.getConfigurationSection("cooldowns").getKeys(false)) {
                long endTime = config.getLong("cooldowns." + key);
                if (System.currentTimeMillis() < endTime) {
                    playerCooldowns.put(key, endTime);
                }
            }
            if (!playerCooldowns.isEmpty()) {
                cooldowns.put(player.getUniqueId(), playerCooldowns);
            }
        }

        // Load Filters
        if (config.contains("filters")) {
            List<String> matNames = config.getStringList("filters");
            List<Material> mats = new ArrayList<>();
            for (String name : matNames) {
                try {
                    mats.add(Material.valueOf(name));
                } catch (IllegalArgumentException ignored) {}
            }
            filters.put(player.getUniqueId(), mats);
        }

        // Load Trap Material
        if (config.contains("trap_material")) {
            try {
                trapMaterials.put(player.getUniqueId(), Material.valueOf(config.getString("trap_material")));
            } catch (Exception ignored) {}
        }
    }

    public void save(Player player) {
        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Save Cooldowns
        config.set("cooldowns", null);
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns != null) {
            for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
                if (System.currentTimeMillis() < entry.getValue()) {
                    config.set("cooldowns." + entry.getKey(), entry.getValue());
                }
            }
        }

        // Save Filters
        config.set("filters", null);
        List<Material> playerFilter = filters.get(player.getUniqueId());
        if (playerFilter != null && !playerFilter.isEmpty()) {
            config.set("filters", playerFilter.stream().map(Material::name).collect(Collectors.toList()));
        }

        // Save Trap Material
        config.set("trap_material", null);
        if (trapMaterials.containsKey(player.getUniqueId())) {
            config.set("trap_material", trapMaterials.get(player.getUniqueId()).name());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unload(Player player) {
        save(player);
        cooldowns.remove(player.getUniqueId());
        filters.remove(player.getUniqueId());
        trapMaterials.remove(player.getUniqueId());
    }

    public void saveAll() {
        for (UUID uuid : cooldowns.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                save(player);
            }
        }
    }
}
