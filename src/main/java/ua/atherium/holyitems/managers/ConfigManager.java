package ua.atherium.holyitems.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.holyitems.HolyWorldItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final HolyWorldItems plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    public ConfigManager(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        loadConfig("config.yml");
        loadConfig("items.yml");
        loadConfig("spheres.yml");
        loadConfig("enchantments.yml");
        loadConfig("potions.yml");
    }

    private void loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(name, config);
        files.put(name, file);
    }

    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    public void saveConfig(String name) {
        try {
            configs.get(name).save(files.get(name));
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить конфигурацию: " + name);
            e.printStackTrace();
        }
    }

    public void reloadConfigs() {
        configs.clear();
        files.clear();
        loadConfigs();
    }
}
