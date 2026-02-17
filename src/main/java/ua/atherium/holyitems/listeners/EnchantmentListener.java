package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomEnchantment;

import java.util.Random;

public class EnchantmentListener implements Listener {

    private final HolyWorldItems plugin;
    private final Random random = new Random();

    public EnchantmentListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int cost = event.getExpLevelCost();

        // Only high level enchants
        if (cost < 30) return;

        for (CustomEnchantment enchant : plugin.getEnchantmentManager().getAllEnchantments()) {
            if (enchant.isVanilla()) continue; // Vanilla handled by game

            // Check applicability
            boolean applicable = false;
            for (String matName : enchant.getAppliesTo()) {
                if (item.getType().name().contains(matName) || (matName.equals("ALL_TOOLS") && isTool(item.getType()))) {
                    applicable = true;
                    break;
                }
            }

            if (applicable) {
                // Chance: 5%
                if (random.nextInt(100) < 5) {
                    plugin.getEnchantmentManager().applyEnchantment(item, enchant.getId(), 1);
                }
            }
        }
    }

    private boolean isTool(Material mat) {
        String name = mat.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }
}
