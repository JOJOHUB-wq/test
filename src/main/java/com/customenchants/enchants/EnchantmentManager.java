package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentManager {

    private final AtheriumEnchants plugin;
    private final List<CustomEnchant> registeredEnchants = new ArrayList<>();

    public EnchantmentManager(AtheriumEnchants plugin) {
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
        registerEnchant(new LavawalkerEnchant(plugin));
        registerEnchant(new ScoutEnchant(plugin));
        registerEnchant(new DetectEnchant(plugin));

        // Misc Tool/Weapon Enchantments
        registerEnchant(new PingerEnchant(plugin));
        registerEnchant(new GreenerEnchant(plugin));
        registerEnchant(new PullingEnchant(plugin));
        registerEnchant(new ComebackEnchant(plugin));
        registerEnchant(new BomberEnchant(plugin));
        registerEnchant(new WebEnchant(plugin));
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

    public CustomEnchant getEnchantByDisplayName(String displayName) {
        for (CustomEnchant enchant : registeredEnchants) {
            if (enchant.getDisplayName().equalsIgnoreCase(displayName)) {
                return enchant;
            }
        }
        return null;
    }

    public CustomEnchant getEnchantByKey(String key) {
        for (CustomEnchant enchant : registeredEnchants) {
            if (enchant.getKey().equalsIgnoreCase(key)) {
                return enchant;
            }
        }
        return null;
    }
}