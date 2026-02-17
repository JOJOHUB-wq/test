package ua.atherium.holyitems.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final HolyWorldItems plugin;
    private final Map<UUID, FileConfiguration> configs = new HashMap<>();
    private final Map<UUID, File> files = new HashMap<>();

    public PlayerDataManager(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    public void loadData(Player player) {
        File folder = new File(plugin.getDataFolder(), "player-data");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        configs.put(player.getUniqueId(), config);
        files.put(player.getUniqueId(), file);

        // Load Cooldowns to Manager
        plugin.getCooldownManager().loadFromConfig(player.getUniqueId(), config);
    }

    public void saveData(Player player) {
        if (!configs.containsKey(player.getUniqueId())) return;

        FileConfiguration config = configs.get(player.getUniqueId());

        // Save Cooldowns from Manager
        plugin.getCooldownManager().saveToConfig(player.getUniqueId(), config);

        try {
            config.save(files.get(player.getUniqueId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        configs.remove(player.getUniqueId());
        files.remove(player.getUniqueId());
    }

    public FileConfiguration getConfig(Player player) {
        return configs.get(player.getUniqueId());
    }

    public Material getTrapMaterial(Player player) {
        FileConfiguration config = getConfig(player);
        if (config == null) return Material.COBWEB;
        String name = config.getString("trap_material", "COBWEB");
        return Material.getMaterial(name) != null ? Material.getMaterial(name) : Material.COBWEB;
    }

    public void setTrapMaterial(Player player, Material mat) {
        FileConfiguration config = getConfig(player);
        if (config != null) {
            config.set("trap_material", mat.name());
        }
    }

    public java.util.List<Material> getFilter(Player player) {
        FileConfiguration config = getConfig(player);
        if (config == null) return new java.util.ArrayList<>();
        java.util.List<String> list = config.getStringList("filter");
        java.util.List<Material> mats = new java.util.ArrayList<>();
        for (String s : list) {
            Material m = Material.getMaterial(s);
            if (m != null) mats.add(m);
        }
        return mats;
    }

    public void setFilter(Player player, java.util.List<Material> mats) {
        FileConfiguration config = getConfig(player);
        if (config != null) {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (Material m : mats) {
                list.add(m.name());
            }
            config.set("filter", list);
        }
    }
}
