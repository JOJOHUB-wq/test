package ua.atherium.holyitems.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadAll();
    }

    public void loadAll() {
        loadConfig("config.yml");
        loadConfig("items.yml");
        loadConfig("spheres.yml");
        loadConfig("enchantments.yml");
        loadConfig("potions.yml");
    }

    private void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        configFiles.put(fileName, file);
    }

    public FileConfiguration getConfig(String fileName) {
        if (!configs.containsKey(fileName)) {
            loadConfig(fileName);
        }
        return configs.get(fileName);
    }

    public void saveConfig(String fileName) {
        if (configs.containsKey(fileName) && configFiles.containsKey(fileName)) {
            try {
                configs.get(fileName).save(configFiles.get(fileName));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config: " + fileName, e);
            }
        }
    }

    public void reloadAll() {
        configs.clear();
        configFiles.clear();
        loadAll();
    }
}
