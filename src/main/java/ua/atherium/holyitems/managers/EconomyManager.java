package ua.atherium.holyitems.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EconomyManager {

    private final JavaPlugin plugin;
    private Economy economy;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!setupEconomy()) {
            plugin.getLogger().warning("Vault not found! Economy features disabled.");
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

    public boolean has(OfflinePlayer player, double amount) {
        if (economy == null) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (economy == null) return;
        economy.depositPlayer(player, amount);
    }

    public boolean isEnabled() {
        return economy != null;
    }

    public String format(double amount) {
        if (economy == null) return String.format("%.2f", amount);
        return economy.format(amount);
    }
}
