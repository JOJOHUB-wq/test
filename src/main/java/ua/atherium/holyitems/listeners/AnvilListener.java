package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    private final HolyWorldItems plugin;

    public AnvilListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        if (first == null || second == null) return;

        String id1 = getSphereId(first);
        String id2 = getSphereId(second);

        if (id1 != null && id2 != null) {
            // Combining two spheres
            String rarity1 = getRarity(first);
            String rarity2 = getRarity(second);

            if (rarity1 == null || rarity2 == null || !rarity1.equals(rarity2)) return;

            // Check convertibility
            // Prompt says: "Mythic sphere (NOT convertible to talisman)".
            // "Combine 2 same rarity spheres in anvil -> higher rarity".
            // If currently COMMON -> EPIC.
            // If EPIC -> LEGENDARY.
            // If LEGENDARY -> MYTHIC?
            // "Mythic sphere... Crafting: Legendary + Epic in anvil".
            // Wait, prompt contradiction?
            // "Combine 2 same rarity spheres -> higher rarity".
            // But "Mythic sphere... Crafting: Legendary + Epic".
            // Let's assume standard upgrade is same rarity -> +1.
            // Except Mythic which is specific recipe.

            String nextRarity = getNextRarity(rarity1);
            if (nextRarity == null) return; // Max level or special

            // Logic: Combine effects
            // Create new item with new rarity and combined effects
            // Result item is tricky because we don't have a template for every combination.
            // We must construct it dynamically.

            ItemStack result = new ItemStack(Material.MAGMA_CREAM);
            ItemMeta meta = result.getItemMeta();

            // Name: Based on rarity
            String color = plugin.getConfigManager().getConfig("spheres.yml").getString("rarity." + nextRarity + ".color", "&f");
            meta.setDisplayName(Utils.color(color + "Сфера " + nextRarity)); // Generic name?

            // Combine Lore / Effects
            // We need to extract effects from items.
            // Attributes are on item meta.
            // Potion effects (Haste) might be in lore or PDC.
            // Since we implemented SphereManager using Attributes for most things, we can copy attributes.

            ItemMeta meta1 = first.getItemMeta();
            ItemMeta meta2 = second.getItemMeta();

            Map<String, Double> combinedAttributes = new java.util.HashMap<>();
            Map<String, org.bukkit.attribute.AttributeModifier.Operation> operations = new java.util.HashMap<>();

            if (meta1.hasAttributeModifiers()) {
                meta1.getAttributeModifiers().forEach((attr, mod) -> {
                    String k = attr.name() + ":" + mod.getOperation().name();
                    combinedAttributes.merge(k, mod.getAmount(), Double::sum);
                    operations.put(k, mod.getOperation());
                });
            }
            if (meta2.hasAttributeModifiers()) {
                meta2.getAttributeModifiers().forEach((attr, mod) -> {
                    String k = attr.name() + ":" + mod.getOperation().name();
                    combinedAttributes.merge(k, mod.getAmount(), Double::sum);
                    operations.put(k, mod.getOperation());
                });
            }

            // Update Lore
            List<String> lore = new ArrayList<>();
            lore.add(Utils.color("&7Редкость: " + plugin.getConfigManager().getConfig("spheres.yml").getString("rarity." + nextRarity + ".name")));

            for (Map.Entry<String, Double> entry : combinedAttributes.entrySet()) {
                String[] parts = entry.getKey().split(":");
                org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf(parts[0]);
                org.bukkit.attribute.AttributeModifier.Operation op = operations.get(entry.getKey());

                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "sphere_combined_" + attr.name().toLowerCase() + "_" + op.name().toLowerCase());
                org.bukkit.attribute.AttributeModifier mod = new org.bukkit.attribute.AttributeModifier(key, entry.getValue(), op, org.bukkit.inventory.EquipmentSlotGroup.OFFHAND);
                meta.addAttributeModifier(attr, mod);

                lore.add(Utils.color("&7" + attr.name() + ": &e+" + entry.getValue()));
            }

            meta.setLore(lore);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(plugin.getSphereManager().getSphereKey(), PersistentDataType.STRING, "custom_combined");
            pdc.set(plugin.getSphereManager().getRarityKey(), PersistentDataType.STRING, nextRarity);

            result.setItemMeta(meta);
            event.setResult(result);
            plugin.getServer().getScheduler().runTask(plugin, () -> event.getInventory().setRepairCost(10)); // Set cost
        } else if (id1 != null && second.getType() == Material.TOTEM_OF_UNDYING) {
             // Conversion to Talisman?
             // Prompt: "Trade with 'Сферомант' (/warp spheres)... Conversion GUI (sphere -> talisman)".
             // Doesn't mention anvil for conversion.
             // "Combine 2 same rarity spheres in anvil -> higher rarity".
             // So conversion is GUI only.
        } else if (first.getType() == Material.DISPENSER && second.getType() == Material.NAME_TAG) {
            // Auto Crafter setup
            // "Rename dispenser in anvil".
            // Just renaming. Vanilla anvil handles it.
        }
    }

    private String getSphereId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getSphereKey(), PersistentDataType.STRING);
    }

    private String getRarity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getRarityKey(), PersistentDataType.STRING);
    }

    private String getNextRarity(String current) {
        return switch (current) {
            case "COMMON" -> "EPIC";
            case "EPIC" -> "LEGENDARY";
            // LEGENDARY + LEGENDARY -> ? Prompt says Mythic is Legendary + Epic.
            // So Legendary + Legendary isn't defined?
            // "System Activation... Combine 2 same rarity...".
            // I'll assume Legendary+Legendary -> Mythic?
            // Or maybe just Common->Epic->Legendary chain.
            default -> null;
        };
    }
}
