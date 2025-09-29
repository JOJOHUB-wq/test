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
        // Weapon Enchantments
        registerEnchant(new VampirismEnchant(plugin));
        registerEnchant(new PoisonEnchant(plugin));
        registerEnchant(new OxidationEnchant(plugin));
        registerEnchant(new GlowEnchant(plugin));
        registerEnchant(new ExperiencedEnchant(plugin));
        registerEnchant(new HunterEnchant(plugin));
        registerEnchant(new StuporEnchant(plugin));
        registerEnchant(new SniperEnchant(plugin));
        registerEnchant(new HawkEnchant(plugin));

        // Tool Enchantments
        registerEnchant(new BulldozerEnchant(plugin));
        registerEnchant(new AutoSmeltEnchant(plugin));
        registerEnchant(new LumberjackEnchant(plugin));

        // Armor Enchantments
        registerEnchant(new DodgeEnchant(plugin));
        registerEnchant(new JumperEnchant(plugin));
        registerEnchant(new PoisonThornsEnchant(plugin));
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