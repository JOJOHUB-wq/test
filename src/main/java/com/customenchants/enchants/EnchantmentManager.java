package com.customenchants.enchants;

import com.customenchants.CustomEnchants;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentManager {

    private final CustomEnchants plugin;
    private final List<CustomEnchant> registeredEnchants = new ArrayList<>();

    public EnchantmentManager(CustomEnchants plugin) {
        this.plugin = plugin;
    }

    public void registerEnchants() {
        // Register enchantments here
        registerEnchant(new VampirismEnchant());
        registerEnchant(new PoisonEnchant());
        registerEnchant(new OxidationEnchant());
        registerEnchant(new GlowEnchant());
        registerEnchant(new ExperiencedEnchant());
        registerEnchant(new HunterEnchant());
        registerEnchant(new StuporEnchant());
        registerEnchant(new SniperEnchant());
        registerEnchant(new HawkEnchant());
    }

    private void registerEnchant(CustomEnchant enchant) {
        registeredEnchants.add(enchant);
        plugin.getServer().getPluginManager().registerEvents(enchant, plugin);
        plugin.getLogger().info("Registered enchant: " + enchant.getName());
    }

    public List<CustomEnchant> getRegisteredEnchants() {
        return registeredEnchants;
    }

    public CustomEnchant getEnchantByName(String name) {
        for (CustomEnchant enchant : registeredEnchants) {
            if (enchant.getName().equalsIgnoreCase(name)) {
                return enchant;
            }
        }
        return null;
    }
}