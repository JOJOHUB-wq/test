package ua.atherium.holyitems.managers;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomPotion;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.*;
import java.util.logging.Level;

public class PotionManager {

    private final HolyWorldItems plugin;
    private final Map<String, CustomPotion> potions = new HashMap<>();
    private final NamespacedKey potionIdKey;

    public PotionManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.potionIdKey = new NamespacedKey(plugin, "potion_id");
        loadPotions();
    }

    public void loadPotions() {
        potions.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("potions.yml");
        ConfigurationSection section = config.getConfigurationSection("potions");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            try {
                ConfigurationSection potionSection = section.getConfigurationSection(id);
                if (potionSection == null) continue;

                String name = potionSection.getString("name");
                String colorStr = potionSection.getString("color");
                List<String> lore = potionSection.getStringList("lore");
                double price = potionSection.getDouble("price");

                List<PotionEffect> effects = new ArrayList<>();
                if (potionSection.contains("effects")) {
                    List<Map<?, ?>> effectsList = potionSection.getMapList("effects");
                    for (Map<?, ?> map : effectsList) {
                        String typeName = (String) map.get("type");
                        int amplifier = (int) map.get("amplifier");
                        int duration = (int) map.get("duration"); // seconds

                        PotionEffectType type = PotionEffectType.getByName(typeName);
                        if (type != null) {
                            effects.add(new PotionEffect(type, duration * 20, amplifier));
                        }
                    }
                }

                CustomPotion potion = new CustomPotion(id, name, colorStr, effects, lore, price);
                potions.put(id, potion);

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load potion " + id, e);
            }
        }
        plugin.getLogger().info("Loaded " + potions.size() + " custom potions.");
    }

    public ItemStack getPotionItem(String id) {
        CustomPotion potion = potions.get(id);
        if (potion == null) return null;

        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(potion.getName()));
            meta.setLore(ChatUtil.color(potion.getLore()));

            if (potion.getColor() != null) {
                try {
                    String hex = potion.getColor().replace("#", "");
                    meta.setColor(Color.fromRGB(Integer.parseInt(hex, 16)));
                } catch (Exception ignored) {}
            }

            for (PotionEffect effect : potion.getEffects()) {
                meta.addCustomEffect(effect, true);
            }

            meta.getPersistentDataContainer().set(potionIdKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    public CustomPotion getPotion(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(potionIdKey, PersistentDataType.STRING);
        if (id == null) return null;
        return potions.get(id);
    }

    public Collection<CustomPotion> getAllPotions() {
        return potions.values();
    }
}
