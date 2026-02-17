package ua.atherium.holyitems.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.Rarity;
import ua.atherium.holyitems.objects.Sphere;
import ua.atherium.holyitems.objects.SphereEffect;
import ua.atherium.holyitems.objects.SphereType;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.*;
import java.util.logging.Level;

public class SphereManager {

    private final HolyWorldItems plugin;
    private final Map<String, Sphere> spheres = new HashMap<>();
    private final NamespacedKey sphereIdKey;
    private final NamespacedKey typeKey;

    public SphereManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.sphereIdKey = new NamespacedKey(plugin, "sphere_id");
        this.typeKey = new NamespacedKey(plugin, "sphere_type");
        loadSpheres();
    }

    public void loadSpheres() {
        spheres.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("spheres.yml");
        ConfigurationSection section = config.getConfigurationSection("spheres");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            try {
                ConfigurationSection sphereSection = section.getConfigurationSection(id);
                if (sphereSection == null) continue;

                Rarity rarity = Rarity.valueOf(sphereSection.getString("rarity", "COMMON").toUpperCase());
                SphereType type = SphereType.valueOf(sphereSection.getString("type", "SPHERE").toUpperCase());
                Material material = Material.valueOf(sphereSection.getString("material", "MAGMA_CREAM").toUpperCase());
                String name = sphereSection.getString("name");
                List<String> lore = sphereSection.getStringList("lore");
                int convertCost = sphereSection.getInt("convert_cost", 0);
                int shardValue = sphereSection.getInt("shard_value", 0);
                boolean convertible = sphereSection.getBoolean("convertible", true);

                List<SphereEffect> effects = new ArrayList<>();
                List<PotionEffect> potionEffects = new ArrayList<>();

                if (sphereSection.contains("effects")) {
                    List<Map<?, ?>> effectsList = sphereSection.getMapList("effects");
                    for (Map<?, ?> map : effectsList) {
                        try {
                            String typeName = ((String) map.get("type")).toUpperCase();
                            if (typeName.equals("FAST_DIGGING") || typeName.equals("HASTE")) {
                                double amount = ((Number) map.get("amount")).doubleValue();
                                potionEffects.add(new PotionEffect(PotionEffectType.HASTE, 40, (int) amount));
                            } else {
                                Attribute attribute = Attribute.valueOf(typeName);
                                double amount = ((Number) map.get("amount")).doubleValue();
                                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(((String) map.get("operation")).toUpperCase());
                                effects.add(new SphereEffect(attribute, amount, operation));
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Invalid effect in sphere " + id + ": " + e.getMessage());
                        }
                    }
                }

                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    if (name != null) meta.setDisplayName(ChatUtil.color(name));
                    if (lore != null) meta.setLore(ChatUtil.color(lore));

                    meta.getPersistentDataContainer().set(sphereIdKey, PersistentDataType.STRING, id);
                    meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());

                    // Apply Attributes
                    for (SphereEffect effect : effects) {
                        AttributeModifier modifier = new AttributeModifier(
                                new NamespacedKey(plugin, "sphere_" + id + "_" + effect.getAttribute().name().toLowerCase()),
                                effect.getAmount(),
                                effect.getOperation(),
                                EquipmentSlotGroup.OFFHAND
                        );
                        meta.addAttributeModifier(effect.getAttribute(), modifier);
                    }

                    itemStack.setItemMeta(meta);
                }

                Sphere sphere = new Sphere(id, rarity, type, itemStack, effects, potionEffects, convertCost, shardValue, convertible);
                spheres.put(id, sphere);

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load sphere " + id, e);
            }
        }
        plugin.getLogger().info("Loaded " + spheres.size() + " spheres.");
    }

    public Sphere getSphere(String id) {
        return spheres.get(id);
    }

    public Sphere getSphere(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(sphereIdKey, PersistentDataType.STRING);
        if (id == null) return null;
        return spheres.get(id);
    }

    public ItemStack getTalismanItem(String id) {
        Sphere sphere = spheres.get(id);
        if (sphere == null) return null;

        ItemStack stack = sphere.getItemStack();
        stack.setType(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String name = meta.getDisplayName();
            meta.setDisplayName(name.replace("Сфера", "Талисман").replace("сфера", "талисман"));
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, SphereType.TALISMAN.name());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public NamespacedKey getSphereIdKey() { return sphereIdKey; }
    public NamespacedKey getTypeKey() { return typeKey; }

    public Collection<Sphere> getAllSpheres() { return spheres.values(); }

    public ItemStack getShardItem(int amount) {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color("&bОсколок сферы"));
            // Add a unique tag to identify valid shards if needed, or rely on name/type
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "shard_item"), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isShard(ItemStack item) {
        if (item == null || item.getType() != Material.PRISMARINE_SHARD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "shard_item"), PersistentDataType.BYTE);
    }

    public String getNextTierId(String id) {
        if (id.startsWith("common_") && id.endsWith("_1")) {
            return id.replace("common_", "epic_").replace("_1", "_2");
        }
        if (id.startsWith("epic_") && id.endsWith("_2")) {
            return id.replace("epic_", "legendary_").replace("_2", "_3");
        }
        return null;
    }
}
