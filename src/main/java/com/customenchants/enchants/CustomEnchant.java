package com.customenchants.enchants;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomEnchant implements Listener {

    public abstract String getName();
    public abstract int getMaxLevel();
    public abstract boolean canEnchantItem(ItemStack item);

    public String getLore(int level) {
        return ChatColor.GRAY + getName() + " " + level;
    }

    public void applyToItem(ItemStack item, int level) {
        if (level <= 0) return;
        if (level > getMaxLevel()) level = getMaxLevel();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(getLore(level));
        meta.setLore(lore);

        // This is a simple way to make the item glow.
        // A more complex implementation would involve custom NBT tags.
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);

        item.setItemMeta(meta);
    }

    public int getLevelFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return 0;

        for (String line : lore) {
            if (line.contains(getName())) {
                // This is a simple parsing method.
                // A more robust solution would be needed for complex level strings.
                String[] parts = ChatColor.stripColor(line).split(" ");
                try {
                    return Integer.parseInt(parts[parts.length - 1]);
                } catch (NumberFormatException e) {
                    return 1; // Default to level 1 if parsing fails
                }
            }
        }
        return 0;
    }
}