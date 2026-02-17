package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.Collections;

public class SphereConverterGUI extends BaseGUI {

    private static final int INPUT_SLOT = 13;
    private static final int CONVERT_SLOT = 22;

    public SphereConverterGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, Utils.color("&8Конвертация сфер"));
        init();
    }

    private void init() {
        // Decor
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < 27; i++) {
            if (i != INPUT_SLOT && i != CONVERT_SLOT && i != 26) {
                inventory.setItem(i, glass);
            }
        }

        updateButton();

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(Utils.color("&cНазад"));
        back.setItemMeta(bMeta);
        inventory.setItem(26, back);
    }

    private void updateButton() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.color("&aКонвертировать"));
        meta.setLore(Collections.singletonList(Utils.color("&7Нажмите, чтобы превратить сферу в талисман")));
        item.setItemMeta(meta);
        inventory.setItem(CONVERT_SLOT, item);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 26) {
            // Return item if present
            ItemStack input = inventory.getItem(INPUT_SLOT);
            if (input != null && input.getType() != Material.AIR) {
                player.getInventory().addItem(input);
            }
            new SphereTraderGUI(plugin, player).open();
            return;
        }

        if (slot == INPUT_SLOT) {
            // Allow interaction?
            // BaseGUI cancels everything.
            // I need to allow picking up/placing.
            event.setCancelled(false);
            // Delay update?
            plugin.getServer().getScheduler().runTask(plugin, this::updateButton);
            return;
        }

        if (slot == CONVERT_SLOT) {
            ItemStack input = inventory.getItem(INPUT_SLOT);
            if (input == null || input.getType() == Material.AIR) {
                player.sendMessage(Utils.color("&cПоложите сферу в слот!"));
                return;
            }

            String id = plugin.getSphereManager().getSphereKey() != null && input.hasItemMeta()
                    ? input.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getSphereKey(), org.bukkit.persistence.PersistentDataType.STRING)
                    : null;

            if (id == null) {
                player.sendMessage(Utils.color("&cЭто не сфера!"));
                return;
            }

            if (input.getType() == Material.TOTEM_OF_UNDYING) {
                player.sendMessage(Utils.color("&cЭто уже талисман!"));
                return;
            }

            // Check cost
            String rarity = input.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getRarityKey(), org.bukkit.persistence.PersistentDataType.STRING);
            int cost = 30; // Common
            if ("EPIC".equals(rarity)) cost = 50;
            if ("LEGENDARY".equals(rarity)) cost = 60;

            if (player.getLevel() < cost) {
                player.sendMessage(Utils.color("&cНедостаточно уровней! Нужно: " + cost));
                return;
            }

            // Convert
            player.setLevel(player.getLevel() - cost);

            input.setType(Material.TOTEM_OF_UNDYING);
            ItemMeta meta = input.getItemMeta();
            // Update name/lore? "Обычная сфера" -> "Обычный талисман"?
            // Prompt: "Common Sphere / Talisman".
            // Name: "Sphere of Damage".
            // I'll replace "Сфера" with "Талисман" in name.
            if (meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName().replace("Сфера", "Талисман"));
            }
            input.setItemMeta(meta);

            player.sendMessage(Utils.color("&aУспешная конвертация!"));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);

            // Item is updated in slot (reference).
            inventory.setItem(INPUT_SLOT, input);
            return;
        }

        // Inventory interaction (Player inventory)
        if (event.getClickedInventory() == player.getInventory()) {
            event.setCancelled(false);
            // If shift click, handle move to input slot?
            if (event.isShiftClick()) {
                // Check if item fits input slot logic?
                // Too complex for quick impl.
                // Just allow moving.
            }
        }
    }
}
