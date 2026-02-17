package ua.atherium.holyitems.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomItem;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemManager {

    private final HolyWorldItems plugin;
    private final Map<String, CustomItem> items = new HashMap<>();
    private final NamespacedKey idKey;

    public ItemManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "custom_item_id");
        loadItems();
    }

    public void loadItems() {
        items.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("items.yml");
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            try {
                ConfigurationSection itemSection = section.getConfigurationSection(id);
                if (itemSection == null) continue;

                // Load properties
                String materialName = itemSection.getString("material");
                ItemStack itemStack;

                if ("PLAYER_HEAD".equalsIgnoreCase(materialName) || "SKULL".equalsIgnoreCase(materialName)) {
                    String texture = itemSection.getString("skull_texture");
                    if (texture != null) {
                        itemStack = plugin.getSkullManager().createSkull(texture);
                    } else {
                        itemStack = new ItemStack(Material.PLAYER_HEAD);
                    }
                } else {
                    Material material = Material.valueOf(materialName);
                    itemStack = new ItemStack(material);
                }

                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    // Name & Lore
                    String name = itemSection.getString("name");
                    if (name != null) meta.setDisplayName(ChatUtil.color(name));

                    List<String> lore = itemSection.getStringList("lore");
                    if (lore != null) meta.setLore(ChatUtil.color(lore));

                    // Custom Model Data
                    if (itemSection.contains("custom_model_data")) {
                        meta.setCustomModelData(itemSection.getInt("custom_model_data"));
                    }

                    // Unbreakable
                    if (itemSection.getBoolean("unbreakable")) {
                        meta.setUnbreakable(true);
                    }

                    // Enchantment Glow (hide enchants)
                    if (itemSection.getBoolean("enchantment_glow")) {
                        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    // Enchantments
                    if (itemSection.contains("enchantments")) {
                        ConfigurationSection enchants = itemSection.getConfigurationSection("enchantments");
                        if (enchants != null) {
                            for (String enchName : enchants.getKeys(false)) {
                                Enchantment enchantment = Enchantment.getByName(enchName.toUpperCase());
                                if (enchantment == null) {
                                    // Try NamespacedKey
                                    try {
                                        enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchName.toLowerCase()));
                                    } catch (Exception ignored) {}
                                }

                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, enchants.getInt(enchName), true);
                                }
                            }
                        }
                    }

                    // Attributes
                    if (itemSection.contains("attributes")) {
                        ConfigurationSection attrs = itemSection.getConfigurationSection("attributes");
                        if (attrs != null) {
                            for (String attrName : attrs.getKeys(false)) {
                                try {
                                    Attribute attribute = Attribute.valueOf(attrName.toUpperCase());
                                    double amount = attrs.getDouble(attrName);
                                    EquipmentSlot slot = getSlot(itemStack.getType());
                                    EquipmentSlotGroup group;
                                    if (slot == EquipmentSlot.HAND) group = EquipmentSlotGroup.HAND;
                                    else if (slot == EquipmentSlot.OFF_HAND) group = EquipmentSlotGroup.OFFHAND;
                                    else if (slot == EquipmentSlot.HEAD) group = EquipmentSlotGroup.HEAD;
                                    else if (slot == EquipmentSlot.CHEST) group = EquipmentSlotGroup.CHEST;
                                    else if (slot == EquipmentSlot.LEGS) group = EquipmentSlotGroup.LEGS;
                                    else if (slot == EquipmentSlot.FEET) group = EquipmentSlotGroup.FEET;
                                    else group = EquipmentSlotGroup.ANY;

                                    AttributeModifier modifier = new AttributeModifier(
                                            new NamespacedKey(plugin, "custom_" + attrName.toLowerCase()),
                                            amount,
                                            AttributeModifier.Operation.ADD_NUMBER,
                                            group
                                    );
                                    meta.addAttributeModifier(attribute, modifier);
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Invalid attribute: " + attrName + " for item " + id);
                                }
                            }
                        }
                    }

                    // Store ID in PDC
                    meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
                    itemStack.setItemMeta(meta);
                }

                // Other properties
                long cooldown = itemSection.getLong("cooldown", 0);
                double price = itemSection.getDouble("price", 0);
                boolean consume = itemSection.getBoolean("consume", false);
                boolean placeable = itemSection.getBoolean("placeable", false);
                String type = itemSection.getString("type", "MISC").toUpperCase();

                // Effects Map (convert section to map)
                Map<String, Object> effects = new HashMap<>();
                if (itemSection.contains("effects")) {
                    ConfigurationSection effectsSection = itemSection.getConfigurationSection("effects");
                    effects.putAll(convertSectionToMap(effectsSection));
                }

                // Add on_hit effects
                if (itemSection.contains("on_hit")) {
                    effects.put("on_hit", convertSectionToMap(itemSection.getConfigurationSection("on_hit")));
                }

                CustomItem customItem = new CustomItem(id, itemStack, cooldown, effects, price, consume, placeable, type);
                items.put(id, customItem);

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load item " + id, e);
            }
        }
        plugin.getLogger().info("Loaded " + items.size() + " custom items.");
    }

    private EquipmentSlot getSlot(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET") || name.endsWith("_HEAD") || name.endsWith("SKULL")) return EquipmentSlot.HEAD;
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) return EquipmentSlot.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlot.LEGS;
        if (name.endsWith("_BOOTS")) return EquipmentSlot.FEET;
        if (name.endsWith("_SHIELD")) return EquipmentSlot.OFF_HAND;
        return EquipmentSlot.HAND; // Default to main hand
    }

    private Map<String, Object> convertSectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();
        if (section == null) return map;
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                map.put(key, convertSectionToMap(section.getConfigurationSection(key)));
            } else {
                map.put(key, section.get(key));
            }
        }
        return map;
    }

    public CustomItem getItem(String id) {
        return items.get(id);
    }

    public CustomItem getItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (id == null) return null;
        return items.get(id);
    }

    public void giveItem(Player player, String id, int amount) {
        CustomItem item = items.get(id);
        if (item != null) {
            ItemStack stack = item.getItemStack();
            stack.setAmount(amount);
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(stack);
            if (!remaining.isEmpty()) {
                for (ItemStack drop : remaining.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
        }
    }

    public Collection<CustomItem> getAllItems() {
        return items.values();
    }

    public NamespacedKey getIdKey() {
        return idKey;
    }
}
