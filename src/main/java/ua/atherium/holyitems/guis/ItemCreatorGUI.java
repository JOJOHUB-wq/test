package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.Collections;

public class ItemCreatorGUI extends BaseGUI {

    public ItemCreatorGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, Utils.color("&8Создание предмета"));
        init();
    }

    private void init() {
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(Utils.color("&eИнструкция"));
        meta.setLore(Collections.singletonList(Utils.color("&7Возьмите предмет в руку и нажмите 'Сохранить'.")));
        info.setItemMeta(meta);
        inventory.setItem(13, info);

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta sMeta = save.getItemMeta();
        sMeta.setDisplayName(Utils.color("&aСохранить предмет в руке"));
        save.setItemMeta(sMeta);
        inventory.setItem(15, save);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getRawSlot() == 15) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage(Utils.color("&cВозьмите предмет в руку!"));
                return;
            }

            // Save to items.yml
            String id = "custom_item_" + System.currentTimeMillis();
            plugin.getConfigManager().getConfig("items.yml").set("items." + id + ".material", hand.getType().name());
            if (hand.hasItemMeta()) {
                if (hand.getItemMeta().hasDisplayName()) {
                    plugin.getConfigManager().getConfig("items.yml").set("items." + id + ".name", hand.getItemMeta().getDisplayName().replace("§", "&"));
                }
                if (hand.getItemMeta().hasLore()) {
                    plugin.getConfigManager().getConfig("items.yml").set("items." + id + ".lore", hand.getItemMeta().getLore());
                }
            }
            plugin.getConfigManager().saveConfig("items.yml");
            plugin.getItemManager().loadItems();

            player.sendMessage(Utils.color("&aПредмет сохранен как " + id));
            player.closeInventory();
        }
    }
}
