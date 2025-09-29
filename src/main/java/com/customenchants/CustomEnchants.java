package com.customenchants;

import com.customenchants.commands.EnchantsCommand;
import com.customenchants.commands.GiveEnchantCommand;
import com.customenchants.enchants.EnchantmentManager;
import com.customenchants.menu.MenuListener;
import com.customenchants.menu.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEnchants extends JavaPlugin {

    private MenuManager menuManager;
    private EnchantmentManager enchantmentManager;

    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        menuManager = new MenuManager(this);
        enchantmentManager = new EnchantmentManager(this);

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        this.getCommand("enchants").setExecutor(new EnchantsCommand(this));
        this.getCommand("giveenchant").setExecutor(new GiveEnchantCommand(this));

        // Load menus after registering command executor and listeners
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
}