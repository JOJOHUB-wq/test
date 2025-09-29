package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AutoSmeltEnchant extends CustomEnchant {

    private final Map<Material, Material> smeltables = new HashMap<>();

    public AutoSmeltEnchant(AtheriumEnchants plugin) {
        super("AutoSmelt", plugin);
        loadSmeltMap();
    }

    private void loadSmeltMap() {
        ConfigurationSection section = plugin.getEnchantmentConfig().getConfig().getConfigurationSection("enchantments." + getName());
        if (section == null) return;

        for (String entry : section.getStringList("smelt_map")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                Material input = Material.matchMaterial(parts[0].trim());
                Material output = Material.matchMaterial(parts[1].trim());
                if (input != null && output != null) {
                    smeltables.put(input, output);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();

        int level = getLevelFromItem(tool);
        if (level <= 0 || player.isSneaking() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Material dropType = smeltables.get(block.getType());
        if (dropType == null) {
            return;
        }

        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int amountToDrop = 1;
        if (fortuneLevel > 0) {
            if (ThreadLocalRandom.current().nextInt(100) < (fortuneLevel * 25)) {
                 amountToDrop += ThreadLocalRandom.current().nextInt(fortuneLevel);
            }
        }

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(dropType, amountToDrop));

        int xpBonus = getConfigValue(level, "xp_bonus", 1);
        event.setExpToDrop(event.getExpToDrop() + xpBonus);
    }
}