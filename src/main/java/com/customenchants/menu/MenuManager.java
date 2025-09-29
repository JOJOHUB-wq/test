package com.customenchants.menu;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {

    private final AtheriumEnchants plugin;
    private final Map<String, FileConfiguration> menuConfigurations = new HashMap<>();

    public MenuManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    public void loadMenus() {
        saveDefaultMenus();

        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (menuFiles == null) {
            return;
        }

        for (File menuFile : menuFiles) {
            String menuId = menuFile.getName().replace(".yml", "");
            FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);
            menuConfigurations.put(menuId, menuConfig);
            plugin.getLogger().info("Loaded menu: " + menuId);
        }
    }

    public void openMenu(Player player, String menuId) {
        FileConfiguration menuConfig = menuConfigurations.get(menuId);
        if (menuConfig == null) {
            player.sendMessage(ChatColor.RED + "Menu not found: " + menuId);
            return;
        }

        String title = ChatColor.translateAlternateColorCodes('&', menuConfig.getString("title", "Menu"));
        int size = menuConfig.getInt("size", 54);

        Menu menu = new Menu(menuId, title, size);
        menu.open(player);

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("items");
        ConfigurationSection animationSection = menuConfig.getConfigurationSection("animation");

        if (animationSection != null && itemsSection != null) {
            new MenuAnimator(player, menu, animationSection, itemsSection, plugin);
        }
    }

    public FileConfiguration getMenuConfig(String id) {
        return menuConfigurations.get(id);
    }

    private void saveDefaultMenus() {
        List<String> defaultMenus = Arrays.asList("enchants.yml", "en1.yml", "en2.yml", "en3.yml");
        for (String menuName : defaultMenus) {
            File menuFile = new File(plugin.getDataFolder(), "menus/" + menuName);
            if (!menuFile.exists()) {
                plugin.saveResource("menus/" + menuName, false);
            }
        }
    }
}