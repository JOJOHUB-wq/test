package ua.atherium.holyitems.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentManager {

    private final HolyWorldItems plugin;
    private final Map<String, CustomEnchantment> enchantments = new HashMap<>();

    public EnchantmentManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        loadEnchantments();
    }

    public void loadEnchantments() {
        enchantments.clear();
        ConfigurationSection section = plugin.getConfigManager().getConfig("enchantments.yml").getConfigurationSection("enchantments");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection ench = section.getConfigurationSection(key);
            String id = ench.getString("id");
            String name = ench.getString("name");
            int maxLevel = ench.getInt("max_level");
            List<String> appliesTo = ench.getStringList("applies_to");
            String description = ench.getString("description");

            enchantments.put(id, new CustomEnchantment(id, name, maxLevel, appliesTo, description, ench));
        }
    }

    public void addEnchantment(ItemStack item, String enchantId, int level) {
        if (item == null || !item.hasItemMeta()) return;
        CustomEnchantment ench = enchantments.get(enchantId);
        if (ench == null) return;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "enchant_" + enchantId.toLowerCase());
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        // Update Lore
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        // Remove old line if exists (simple check)
        String colorName = Utils.color(ench.name);
        lore.removeIf(line -> line.startsWith(colorName));

        String levelStr = level == 1 && ench.maxLevel == 1 ? "" : " " + toRoman(level);
        lore.add(Utils.color(ench.name + levelStr));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public int getEnchantLevel(ItemStack item, String enchantId) {
        if (item == null || !item.hasItemMeta()) return 0;
        NamespacedKey key = new NamespacedKey(plugin, "enchant_" + enchantId.toLowerCase());
        Integer level = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public Map<String, CustomEnchantment> getEnchantments() {
        return enchantments;
    }

    public static class CustomEnchantment {
        public String id;
        public String name;
        public int maxLevel;
        public List<String> appliesTo;
        public String description;
        public ConfigurationSection config; // Store full config for extra properties like 'chance'

        public CustomEnchantment(String id, String name, int maxLevel, List<String> appliesTo, String description, ConfigurationSection config) {
            this.id = id;
            this.name = name;
            this.maxLevel = maxLevel;
            this.appliesTo = appliesTo;
            this.description = description;
            this.config = config;
        }
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(n);
        };
    }
}
