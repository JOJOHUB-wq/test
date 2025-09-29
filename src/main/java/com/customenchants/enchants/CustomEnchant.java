package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CustomEnchant implements Listener {

    protected final AtheriumEnchants plugin;
    private final String key;
    protected final ConfigurationSection configSection;

    public CustomEnchant(String key, AtheriumEnchants plugin) {
        this.key = key;
        this.plugin = plugin;
        this.configSection = plugin.getEnchantmentConfig().getConfig().getConfigurationSection("enchantments." + key);
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return configSection != null && configSection.getBoolean("enabled", false);
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', configSection.getString("name", getKey()));
    }

    public int getMaxLevel() {
        return configSection.getInt("max_level", 1);
    }

    public List<String> getConflicts() {
        return configSection.getStringList("conflicts").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public boolean canEnchantItem(ItemStack item) {
        List<String> targetItems = configSection.getStringList("target_items");
        String itemType = item.getType().name();

        for (String target : targetItems) {
            target = target.toUpperCase();
            if (itemType.endsWith("_" + target) || (target.equals("ARMOR") && (itemType.endsWith("_HELMET") || itemType.endsWith("_CHESTPLATE") || itemType.endsWith("_LEGGINGS") || itemType.endsWith("_BOOTS")))) {
                 return true;
            }
             if (target.equals(item.getType().toString())) return true;
        }
        return false;
    }

    public void applyToItem(ItemStack item, int level) {
        if (level <= 0) return;
        if (level > getMaxLevel()) level = getMaxLevel();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();
        lore.add(getDisplayName() + " " + RomanNumerals.toRoman(level));

        if (item.getType() == Material.ENCHANTED_BOOK) {
            List<String> description = configSection.getStringList("description");
            if (!description.isEmpty()) {
                lore.add("");
                for (String line : description) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + line));
                }
            }
        }

        if(meta.hasLore()) {
            List<String> oldLore = meta.getLore();
            oldLore.removeIf(line -> ChatColor.stripColor(line).startsWith(ChatColor.stripColor(getDisplayName())));
            lore.addAll(oldLore);
        }

        meta.setLore(lore);

        if (!meta.hasEnchants()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
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

        String strippedDisplayName = ChatColor.stripColor(getDisplayName());

        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith(strippedDisplayName)) {
                String[] parts = strippedLine.split(" ");
                if (parts.length > 1) {
                    try {
                        return RomanNumerals.romanToArabic(parts[parts.length - 1]);
                    } catch (IllegalArgumentException e) {
                        // Not a roman numeral, ignore
                    }
                }
            }
        }
        return 0;
    }

    public static class RomanNumerals {
        private static final LinkedHashMap<String, Integer> romanMap = new LinkedHashMap<>();
        static {
            romanMap.put("M", 1000);
            romanMap.put("CM", 900);
            romanMap.put("D", 500);
            romanMap.put("CD", 400);
            romanMap.put("C", 100);
            romanMap.put("XC", 90);
            romanMap.put("L", 50);
            romanMap.put("XL", 40);
            romanMap.put("X", 10);
            romanMap.put("IX", 9);
            romanMap.put("V", 5);
            romanMap.put("IV", 4);
            romanMap.put("I", 1);
        }

        public static String toRoman(int number) {
            if (number < 1 || number > 10) return String.valueOf(number); // Only handle up to 10 for enchants
            StringBuilder res = new StringBuilder();
            for (Map.Entry<String, Integer> entry : romanMap.entrySet()) {
                int matches = number / entry.getValue();
                res.append(repeat(entry.getKey(), matches));
                number = number % entry.getValue();
            }
            return res.toString();
        }

        public static int romanToArabic(String roman) {
            int result = 0;
            String romanUpper = roman.toUpperCase();
            for (Map.Entry<String, Integer> entry : romanMap.entrySet()) {
                while (romanUpper.startsWith(entry.getKey())) {
                    result += entry.getValue();
                    romanUpper = romanUpper.substring(entry.getKey().length());
                }
            }
            return result;
        }

        private static String repeat(String s, int n) {
            if (s == null) return null;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(s);
            }
            return sb.toString();
        }
    }

    protected double getConfigValue(int level, String key, double defaultValue) {
        return configSection.getDouble("levels." + level + "." + key, defaultValue);
    }

    protected int getConfigValue(int level, String key, int defaultValue) {
        return configSection.getInt("levels." + level + "." + key, defaultValue);
    }

    protected boolean getConfigValue(int level, String key, boolean defaultValue) {
        return configSection.getBoolean("levels." + level + "." + key, defaultValue);
    }

    protected List<String> getConfigStringList(int level, String key) {
        return configSection.getStringList("levels." + level + "." + key);
    }
}