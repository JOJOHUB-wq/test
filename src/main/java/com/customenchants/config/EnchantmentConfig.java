package com.customenchants.config;

import com.customenchants.CustomEnchants;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class EnchantmentConfig {

    private final CustomEnchants plugin;
    private FileConfiguration config;
    private File configFile;

    public EnchantmentConfig(CustomEnchants plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "enchants.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "enchants.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("enchants.yml", false);
        }
    }
}