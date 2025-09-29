package com.customenchants.economy;

import com.customenchants.AtheriumEnchants;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final AtheriumEnchants plugin;
    private Economy economy = null;

    public EconomyManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        if (!setupEconomy()) {
            plugin.getLogger().warning("Vault or an economy plugin not found! Purchase functionality will be disabled.");
        }
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (!hasEconomy()) return 0;
        return economy.getBalance(player);
    }

    public boolean hasEnough(Player player, double amount) {
        if (!hasEconomy()) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!hasEconomy()) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
}