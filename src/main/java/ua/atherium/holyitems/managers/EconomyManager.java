package ua.atherium.holyitems.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import ua.atherium.holyitems.HolyWorldItems;

public class EconomyManager {

    private final HolyWorldItems plugin;
    private Economy economy;

    public EconomyManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        setupEconomy();
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

    public boolean hasMoney(String playerName, double amount) {
        if (economy == null) return false;
        return economy.getBalance(playerName) >= amount;
    }

    public boolean withdraw(String playerName, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    public void deposit(String playerName, double amount) {
        if (economy == null) return;
        economy.depositPlayer(playerName, amount);
    }
}
