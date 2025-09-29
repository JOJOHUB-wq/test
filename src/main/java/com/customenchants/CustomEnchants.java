package com.customenchants;

import com.customenchants.commands.EnchantsCommand;
import com.customenchants.commands.GiveEnchantCommand;
import com.customenchants.config.EnchantmentConfig;
import com.customenchants.enchants.EnchantmentManager;
import com.customenchants.listeners.EnchantingTableListener;
import com.customenchants.menu.MenuListener;
import com.customenchants.menu.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEnchants extends JavaPlugin {

    private MenuManager menuManager;
    private EnchantmentManager enchantmentManager;
    private EnchantmentConfig enchantmentConfig;

    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize configurations
        enchantmentConfig = new EnchantmentConfig(this);

        // Initialize managers
        menuManager = new MenuManager(this);
        enchantmentManager = new EnchantmentManager(this);

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantingTableListener(this), this);
        this.getCommand("enchants").setExecutor(new EnchantsCommand(this));
        this.getCommand("giveenchant").setExecutor(new GiveEnchantCommand(this));

        // Load data
        menuManager.loadMenus();
        enchantmentManager.registerEnchants();

        getLogger().info("CustomEnchants has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomEnchants has been disabled!");
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public EnchantmentConfig getEnchantmentConfig() {
        return enchantmentConfig;
    }
}