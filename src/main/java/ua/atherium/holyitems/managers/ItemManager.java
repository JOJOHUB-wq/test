package ua.atherium.holyitems.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.*;

public class ItemManager {

    private final HolyWorldItems plugin;
    private final Map<String, ItemStack> items = new HashMap<>();
    private final NamespacedKey itemKey;

    public ItemManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "holy_item_id");
        loadItems();
    }

    public void loadItems() {
        items.clear();
        // Clear recipes to avoid dupes on reload (not perfect but works for now)
        // Bukkit.resetRecipes() only clears all... better to use NamespacedKey for recipes.

        FileConfiguration config = plugin.getConfigManager().getConfig("items.yml");
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection itemSection = section.getConfigurationSection(key);
                ItemStack item = createItem(key, itemSection);
                items.put(key, item);

                // Recipes
                registerRecipes(key, item, itemSection);

            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при загрузке предмета " + key + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private ItemStack createItem(String id, ConfigurationSection section) {
        String materialName = section.getString("material");
        Material material = Material.valueOf(materialName);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Name & Lore
        if (section.contains("name")) {
            meta.setDisplayName(Utils.color(section.getString("name")));
        }
        if (section.contains("lore")) {
            meta.setLore(Utils.color(section.getStringList("lore")));
        }

        // Custom Model Data
        if (section.contains("custom_model_data")) {
            meta.setCustomModelData(section.getInt("custom_model_data"));
        }

        // Unbreakable
        if (section.getBoolean("unbreakable")) {
            meta.setUnbreakable(true);
        }

        // Glow
        if (section.getBoolean("enchantment_glow")) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Vanilla Enchantments
        if (section.contains("enchantments")) {
            ConfigurationSection enchants = section.getConfigurationSection("enchantments");
            for (String enchName : enchants.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(enchName.toUpperCase());
                if (enchantment != null) {
                    meta.addEnchant(enchantment, enchants.getInt(enchName), true);
                } else {
                    // Custom enchant logic could go here, but usually stored in NBT/Lore
                }
            }
        }

        // Attributes
        if (section.contains("attributes")) {
            ConfigurationSection attrs = section.getConfigurationSection("attributes");
            for (String attrName : attrs.getKeys(false)) {
                try {
                    Attribute attribute = Attribute.valueOf(attrName);
                    double value = attrs.getDouble(attrName);
                    // Add modifier using NamespacedKey for 1.21+
                    NamespacedKey key = new NamespacedKey(plugin, "holy_attr_" + attrName.toLowerCase());
                    AttributeModifier modifier = new AttributeModifier(key, value, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.ANY);
                    meta.addAttributeModifier(attribute, modifier);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный атрибут: " + attrName);
                }
            }
        }

        // Persistent Data (ID)
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    private void registerRecipes(String id, ItemStack result, ConfigurationSection section) {
        // Craft Recipe (Shaped)
        if (section.contains("craft_recipe")) {
            NamespacedKey key = new NamespacedKey(plugin, "recipe_" + id);
            // Remove old if exists (hard to do without iterating all recipes, skipping for now)

            ConfigurationSection craft = section.getConfigurationSection("craft_recipe");
            ShapedRecipe recipe = new ShapedRecipe(key, result);

            List<String> shape = craft.getStringList("shape");
            recipe.shape(shape.toArray(new String[0]));

            ConfigurationSection ingredients = craft.getConfigurationSection("ingredients");
            for (String charKey : ingredients.getKeys(false)) {
                Material mat = Material.valueOf(ingredients.getString(charKey));
                recipe.setIngredient(charKey.charAt(0), mat);
            }

            // Register safely
            try {
                Bukkit.addRecipe(recipe);
            } catch (Exception ignored) {} // Duplicate key
        }

        // Craft From (Shapeless - e.g. Emerald Dust)
        if (section.contains("craft_from")) {
            NamespacedKey key = new NamespacedKey(plugin, "recipe_shapeless_" + id);
            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            int amount = section.getInt("craft_amount", 1);
            result.setAmount(amount);

            Material mat = Material.valueOf(section.getString("craft_from"));
            recipe.addIngredient(mat);

             try {
                Bukkit.addRecipe(recipe);
            } catch (Exception ignored) {}
        }
    }

    public ItemStack getItem(String id) {
        if (items.containsKey(id)) {
            return items.get(id).clone();
        }
        return null;
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    public Map<String, ItemStack> getItems() {
        return items;
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }
}
