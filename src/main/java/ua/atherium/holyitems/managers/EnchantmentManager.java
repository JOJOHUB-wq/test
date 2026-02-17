package ua.atherium.holyitems.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomEnchantment;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class EnchantmentManager {

    private final HolyWorldItems plugin;
    private final Map<String, CustomEnchantment> enchantments = new HashMap<>();

    public EnchantmentManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        loadEnchantments();
    }

    public void loadEnchantments() {
        enchantments.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("enchantments.yml");
        ConfigurationSection section = config.getConfigurationSection("enchantments");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection enchSection = section.getConfigurationSection(key);
                if (enchSection == null) continue;

                String id = enchSection.getString("id");
                String name = enchSection.getString("name");
                int maxLevel = enchSection.getInt("max_level");
                List<String> appliesTo = enchSection.getStringList("applies_to");
                String description = enchSection.getString("description");
                String vanillaEnchant = enchSection.getString("vanilla_enchant");

                Map<String, Object> effect = new HashMap<>();
                if (enchSection.contains("effect")) {
                    effect.putAll(enchSection.getConfigurationSection("effect").getValues(true));
                }

                CustomEnchantment enchantment = new CustomEnchantment(id, name, maxLevel, appliesTo, description, effect, vanillaEnchant);
                enchantments.put(id, enchantment);

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load enchantment " + key, e);
            }
        }
        plugin.getLogger().info("Loaded " + enchantments.size() + " custom enchantments.");
    }

    public CustomEnchantment getEnchantment(String id) {
        return enchantments.get(id);
    }

    public void applyEnchantment(ItemStack item, String enchantId, int level) {
        CustomEnchantment enchant = getEnchantment(enchantId);
        if (enchant == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (enchant.isVanilla()) {
            Enchantment vanilla = Enchantment.getByName(enchant.getVanillaEnchant().toUpperCase());
            if (vanilla != null) {
                meta.addEnchant(vanilla, level, true);
            }
        } else {
            // Custom Enchantment Logic
            // Add to Lore
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            // Remove existing lore line for this enchant if present?
            // Simple check to prevent duplicates if applied twice?
            // For now assume clean application.

            String roman = toRoman(level);
            String line = ChatUtil.color(enchant.getName() + " " + roman);
            lore.add(line);
            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(plugin, "enchant_" + enchantId.toLowerCase());
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
        }
        item.setItemMeta(meta);
    }

    public int getEnchantLevel(ItemStack item, String enchantId) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();

        CustomEnchantment enchant = getEnchantment(enchantId);
        if (enchant != null && enchant.isVanilla()) {
             Enchantment vanilla = Enchantment.getByName(enchant.getVanillaEnchant().toUpperCase());
             if (vanilla != null) return meta.getEnchantLevel(vanilla);
        }

        NamespacedKey key = new NamespacedKey(plugin, "enchant_" + enchantId.toLowerCase());
        Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public boolean hasEnchant(ItemStack item, String enchantId) {
        return getEnchantLevel(item, enchantId) > 0;
    }

    public String toRoman(int level) {
        if (level <= 0) return "";
        if (level == 1) return "I";
        if (level == 2) return "II";
        if (level == 3) return "III";
        if (level == 4) return "IV";
        if (level == 5) return "V";
        if (level == 6) return "VI";
        if (level == 7) return "VII";
        if (level == 8) return "VIII";
        if (level == 9) return "IX";
        if (level == 10) return "X";
        return String.valueOf(level);
    }

    public java.util.Collection<CustomEnchantment> getAllEnchantments() {
        return enchantments.values();
    }
}
