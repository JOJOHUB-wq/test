package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CustomEnchant implements Listener {

    protected final AtheriumEnchants plugin;
    private final String name;
    private final ConfigurationSection configSection;

    public CustomEnchant(String name, AtheriumEnchants plugin) {
        this.name = name;
        this.plugin = plugin;
        this.configSection = plugin.getEnchantmentConfig().getConfig().getConfigurationSection("enchantments." + name);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return configSection.getString("display_name", getName());
    }

    public int getMaxLevel() {
        return configSection.getInt("max_level", 1);
    }

    public boolean isEnabled() {
        return configSection != null && configSection.getBoolean("enabled", false);
    }

    public List<Material> getApplicableItems() {
        return configSection.getStringList("applicable_items").stream()
                .map(Material::matchMaterial)
                .collect(Collectors.toList());
    }

    public boolean canEnchantItem(ItemStack item) {
        return getApplicableItems().contains(item.getType());
    }

    public boolean isEnchantingTableEnabled() {
        return configSection.getBoolean("enchanting_table_enabled", false);
    }

    public double getEnchantingChance() {
        return configSection.getDouble("enchanting_chance", 0.0);
    }

    public String getLore(int level) {
        return ChatColor.GRAY + getDisplayName() + " " + level;
    }

    public void applyToItem(ItemStack item, int level) {
        if (level <= 0) return;
        if (level > getMaxLevel()) level = getMaxLevel();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        // Remove old lore if present
        lore.removeIf(line -> line.contains(getDisplayName()));
        lore.add(getLore(level));
        meta.setLore(lore);

        // This is a simple way to make the item glow without showing the enchant.
        if (!meta.hasEnchants()) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
    }

    public int getLevelFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            if (ChatColor.stripColor(line).startsWith(getDisplayName())) {
                String[] parts = ChatColor.stripColor(line).split(" ");
                try {
                    return Integer.parseInt(parts[parts.length - 1]);
                } catch (NumberFormatException e) {
                    return 1; // Default to level 1 if parsing fails
                }
            }
        }
        return 0;
    }

    protected double getConfigValue(int level, String key, double defaultValue) {
        return configSection.getDouble("levels." + level + "." + key, defaultValue);
    }

    protected int getConfigValue(int level, String key, int defaultValue) {
        return configSection.getInt("levels." + level + "." + key, defaultValue);
    }
}