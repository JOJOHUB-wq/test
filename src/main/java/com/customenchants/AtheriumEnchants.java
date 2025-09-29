package com.customenchants;

import com.customenchants.commands.AetheriumTabCompleter;
import com.customenchants.commands.CommandManager;
import com.customenchants.config.EnchantmentConfig;
import com.customenchants.economy.EconomyManager;
import com.customenchants.enchants.EnchantmentManager;
import com.customenchants.listeners.AnvilListener;
import com.customenchants.listeners.EnchantingTableListener;
import com.customenchants.menu.MenuListener;
import com.customenchants.menu.MenuManager;
import com.customenchants.utils.WorldGuardUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class AtheriumEnchants extends JavaPlugin {

    private MenuManager menuManager;
    private EnchantmentManager enchantmentManager;
    private EnchantmentConfig enchantmentConfig;
    private EconomyManager economyManager;
    private WorldGuardUtil worldGuardUtil;

    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize configurations
        enchantmentConfig = new EnchantmentConfig(this);

        // Initialize managers
        economyManager = new EconomyManager(this);
        menuManager = new MenuManager(this);
        enchantmentManager = new EnchantmentManager(this);
        worldGuardUtil = new WorldGuardUtil();

        // Register listeners and commands
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantingTableListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakHandler(this), this);
        getCommand("ae").setExecutor(new CommandManager(this));
        getCommand("ae").setTabCompleter(new AetheriumTabCompleter(this));

        // Load data
        menuManager.loadMenus();
        enchantmentManager.registerEnchants();

        getLogger().info("AtheriumEnchants has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AtheriumEnchants has been disabled!");
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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public WorldGuardUtil getWorldGuardUtil() {
        return worldGuardUtil;
    }
}