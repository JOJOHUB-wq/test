package com.customenchants.menu;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuAnimator extends BukkitRunnable {

    private final Player player;
    private final Menu menu;
    private final ConfigurationSection animationSection;
    private final ConfigurationSection itemsSection;
    private final AtheriumEnchants plugin;
    private int tick = 1;

    public MenuAnimator(Player player, Menu menu, ConfigurationSection animationSection, ConfigurationSection itemsSection, AtheriumEnchants plugin) {
        this.player = player;
        this.menu = menu;
        this.animationSection = animationSection;
        this.itemsSection = itemsSection;
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (!player.getOpenInventory().getTopInventory().equals(menu.getInventory())) {
            this.cancel();
            return;
        }

        ConfigurationSection tickSection = animationSection.getConfigurationSection(String.valueOf(tick));
        if (tickSection == null) {
            if (tick > animationSection.getKeys(false).size() + 10) {
                this.cancel();
            }
            tick++;
            return;
        }

        List<String> opcodes = tickSection.getStringList("opcodes");
        for (String opcode : opcodes) {
            String[] opcodeParts = opcode.split(":", 2);
            if (opcodeParts.length != 2) continue;

            String operation = opcodeParts[0].trim();
            String argumentStr = opcodeParts[1].trim();

            if (operation.equalsIgnoreCase("set")) {
                String[] args = argumentStr.split("\\s+", 2);
                if (args.length < 2) continue;
                String itemName = args[0];
                String[] slots = args[1].split(",");
                setItem(itemName, slots);
            }
        }
        tick++;
    }

    private void setItem(String itemName, String[] slots) {
        ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemName);
        if (itemConfig == null) return;

        Material material = Material.matchMaterial(itemConfig.getString("material", "STONE"));
        ItemStack itemStack = new ItemStack(material != null ? material : Material.STONE);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            String displayName = ChatColor.translateAlternateColorCodes('&', itemConfig.getString("display_name", ""));
            itemMeta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            if (material == Material.ENCHANTED_BOOK) {
                CustomEnchant enchant = plugin.getEnchantmentManager().getEnchantByKey(itemName);
                if (enchant != null) {
                    ConfigurationSection enchantConfig = plugin.getEnchantmentConfig().getConfig().getConfigurationSection("enchantments." + enchant.getKey());

                    lore.add("");
                    enchantConfig.getStringList("description").forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + line)));
                    lore.add("");
                    lore.add(ChatColor.GRAY + "Редкость: " + ChatColor.translateAlternateColorCodes('&', enchantConfig.getString("rarity_display", enchantConfig.getString("rarity"))));
                    lore.add(ChatColor.GRAY + "Макс. уровень: " + ChatColor.WHITE + CustomEnchant.RomanNumerals.toRoman(enchantConfig.getInt("max_level")));
                    lore.add("");

                    if (enchantConfig.getBoolean("purchase.enabled", false)) {
                        lore.add(ChatColor.GRAY + "Цена: " + ChatColor.GREEN + enchantConfig.getDouble("purchase.price"));
                        lore.add(ChatColor.YELLOW + "Нажмите, чтобы купить!");
                    } else {
                         lore.add(ChatColor.RED + "Нельзя купить.");
                    }
                }
            } else {
                lore.addAll(itemConfig.getStringList("lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList()));
            }
            itemMeta.setLore(lore);

            if (itemConfig.getBoolean("enchanted", false)) {
                itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
        }

        for (String slotStr : slots) {
            if (slotStr.contains("-")) {
                String[] range = slotStr.split("-");
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                for (int i = start; i <= end; i++) {
                    menu.getInventory().setItem(i, itemStack);
                    menu.setSlotMapping(i, itemName);
                }
            } else {
                int slot = Integer.parseInt(slotStr);
                menu.getInventory().setItem(slot, itemStack);
                menu.setSlotMapping(slot, itemName);
            }
        }
    }
}