package ua.atherium.holyitems.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.*;

public class SphereManager {

    private final HolyWorldItems plugin;
    private final Map<String, ItemStack> spheres = new HashMap<>();
    private final NamespacedKey sphereKey;
    private final NamespacedKey rarityKey;

    public SphereManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.sphereKey = new NamespacedKey(plugin, "sphere_id");
        this.rarityKey = new NamespacedKey(plugin, "sphere_rarity");
        loadSpheres();
        startTask();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (offhand != null && offhand.hasItemMeta()) {
                    org.bukkit.persistence.PersistentDataContainer pdc = offhand.getItemMeta().getPersistentDataContainer();
                    // Scan keys? No, keys are namespaced.
                    // Iterating keys in PDC is not directly exposed easily in API?
                    // Actually `pdc.getKeys()` exists.
                    for (NamespacedKey key : pdc.getKeys()) {
                        if (key.getKey().startsWith("sphere_effect_")) {
                            String effectName = key.getKey().replace("sphere_effect_", "").toUpperCase();
                            org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(effectName);
                            if (type != null) {
                                Integer amplifier = pdc.get(key, PersistentDataType.INTEGER);
                                if (amplifier != null) {
                                    // Apply effect
                                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, 40, amplifier, false, false, true)); // 2s duration
                                }
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    public void loadSpheres() {
        spheres.clear();
        ConfigurationSection section = plugin.getConfigManager().getConfig("spheres.yml").getConfigurationSection("spheres");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ItemStack sphere = createSphereItem(key, section.getConfigurationSection(key));
            if (sphere != null) {
                spheres.put(key, sphere);
            }
        }
    }

    private ItemStack createSphereItem(String id, ConfigurationSection section) {
        String rarity = section.getString("rarity", "COMMON");
        Material mat = Material.valueOf(plugin.getConfigManager().getConfig("spheres.yml").getString("rarity." + rarity + ".material_sphere", "MAGMA_CREAM"));

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String name = section.getString("name", "&7Сфера");
        meta.setDisplayName(Utils.color(name));

        List<String> lore = new ArrayList<>();
        lore.add(Utils.color("&7Редкость: " + plugin.getConfigManager().getConfig("spheres.yml").getString("rarity." + rarity + ".name")));

        // Effects
        // The prompt format is slightly different in two places.
        // 1. "effects: - type: GENERIC_ATTACK_DAMAGE amount: 1.0"
        // 2. "type: ATTRIBUTE attribute: GENERIC_ATTACK_DAMAGE amount: 1.0" (my spheres.yml)
        // I should support both or stick to one. My spheres.yml used the second format for simple spheres and first for unique.

        if (section.contains("effects")) {
            // List format
            List<Map<?, ?>> effects = section.getMapList("effects");
            for (Map<?, ?> effect : effects) {
                 String type = (String) effect.get("type");
                 double amount = (Double) effect.get("amount");
                 lore.add(Utils.color("&7" + type + ": &e+" + amount));

                 // Apply attribute if valid
                 try {
                     Attribute attr = Attribute.valueOf(type);
                     String opStr = (String) effect.getOrDefault("operation", "ADD_NUMBER");
                     AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(opStr);

                     NamespacedKey key = new NamespacedKey(plugin, "sphere_" + id + "_" + type.toLowerCase() + "_" + op.name().toLowerCase());
                     AttributeModifier mod = new AttributeModifier(key, amount, op, EquipmentSlotGroup.OFFHAND);
                     meta.addAttributeModifier(attr, mod);
                 } catch (Exception ignored) {
                     // Try Potion Effect
                     org.bukkit.potion.PotionEffectType pet = org.bukkit.potion.PotionEffectType.getByName(type);
                     if (pet != null) {
                         NamespacedKey key = new NamespacedKey(plugin, "sphere_effect_" + type.toLowerCase());
                         meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (int) amount);
                     }
                 }
            }
        } else if (section.contains("attribute")) {
            // Single attribute format
            String attrName = section.getString("attribute");
            double amount = section.getDouble("amount");
            String opStr = section.getString("operation", "ADD_NUMBER");

            lore.add(Utils.color("&7" + attrName + ": &e+" + amount));

             try {
                 Attribute attr = Attribute.valueOf(attrName);
                 AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(opStr);

                 NamespacedKey key = new NamespacedKey(plugin, "sphere_" + id + "_" + attrName.toLowerCase() + "_" + op.name().toLowerCase());
                 AttributeModifier mod = new AttributeModifier(key, amount, op, EquipmentSlotGroup.OFFHAND);
                 meta.addAttributeModifier(attr, mod);
             } catch (Exception ignored) {
                 // Try Potion Effect
                 org.bukkit.potion.PotionEffectType pet = org.bukkit.potion.PotionEffectType.getByName(attrName);
                 if (pet != null) {
                     NamespacedKey key = new NamespacedKey(plugin, "sphere_effect_" + attrName.toLowerCase());
                     meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (int) amount);
                 }
             }
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(sphereKey, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, rarity);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getSphere(String id) {
        if (spheres.containsKey(id)) return spheres.get(id).clone();
        return null;
    }

    public Map<String, ItemStack> getSpheres() {
        return spheres;
    }

    public NamespacedKey getSphereKey() {
        return sphereKey;
    }

    public NamespacedKey getRarityKey() {
        return rarityKey;
    }

    public void registerRecipes() {
        // Register furnace recipes for base materials to allow smelting event to fire
        // Result is dummy, replaced in listener
        org.bukkit.inventory.ItemStack result = new org.bukkit.inventory.ItemStack(Material.PRISMARINE_SHARD);

        NamespacedKey key1 = new NamespacedKey(plugin, "sphere_smelt_magma");
        org.bukkit.inventory.FurnaceRecipe recipe1 = new org.bukkit.inventory.FurnaceRecipe(key1, result, Material.MAGMA_CREAM, 0f, 200);
        try {
            plugin.getServer().addRecipe(recipe1);
        } catch (Exception ignored) {}

        NamespacedKey key2 = new NamespacedKey(plugin, "sphere_smelt_totem");
        org.bukkit.inventory.FurnaceRecipe recipe2 = new org.bukkit.inventory.FurnaceRecipe(key2, result, Material.TOTEM_OF_UNDYING, 0f, 200);
        try {
            plugin.getServer().addRecipe(recipe2);
        } catch (Exception ignored) {}
    }
}
