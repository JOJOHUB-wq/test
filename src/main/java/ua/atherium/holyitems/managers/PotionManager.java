package ua.atherium.holyitems.managers;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class PotionManager {

    private final HolyWorldItems plugin;
    private final Map<String, ItemStack> potions = new HashMap<>();
    private final NamespacedKey potionKey;

    public PotionManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.potionKey = new NamespacedKey(plugin, "custom_potion");
        loadPotions();
    }

    public void loadPotions() {
        potions.clear();
        ConfigurationSection section = plugin.getConfigManager().getConfig("potions.yml").getConfigurationSection("potions");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ItemStack potion = createPotion(key, section.getConfigurationSection(key));
            if (potion != null) {
                potions.put(key, potion);
            }
        }
    }

    private ItemStack createPotion(String id, ConfigurationSection section) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        String name = section.getString("name", "Potion");
        meta.setDisplayName(Utils.color(name));

        String colorHex = section.getString("color", "#FFFFFF");
        try {
            if (colorHex.startsWith("#")) {
                meta.setColor(Color.fromRGB(Integer.valueOf(colorHex.substring(1), 16)));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Неверный цвет зелья: " + colorHex);
        }

        // Effects
        // The prompt says: "effects: - STRENGTH:2:180" (type:amplifier:duration_seconds)
        // OR my potions.yml format: "- type: STRENGTH duration: 180 amplifier: 1"
        // I will support my potions.yml format as I wrote it.

        if (section.contains("effects")) {
             // If list of strings
             if (section.isList("effects") && !section.getMapList("effects").isEmpty()) {
                 // Map list format
                  for (Map<?, ?> effectMap : section.getMapList("effects")) {
                      String typeName = (String) effectMap.get("type");
                      int duration = (Integer) effectMap.get("duration");
                      int amplifier = (Integer) effectMap.get("amplifier");

                      PotionEffectType type = PotionEffectType.getByName(typeName);
                      if (type != null) {
                          meta.addCustomEffect(new PotionEffect(type, duration * 20, amplifier), true);
                      }
                  }
             }
        }

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Often custom potions hide default text
        meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getPotion(String id) {
        if (potions.containsKey(id)) return potions.get(id).clone();
        return null;
    }

    public Map<String, ItemStack> getPotions() {
        return potions;
    }
}
