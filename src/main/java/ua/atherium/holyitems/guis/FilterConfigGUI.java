package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterConfigGUI extends BaseGUI {

    public FilterConfigGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 54, "&8Настройка фильтра");
        loadFilter();
    }

    private void loadFilter() {
        List<Material> filter = plugin.getCooldownManager().getFilter(player);
        inventory.clear();

        for (int i = 0; i < filter.size() && i < 54; i++) {
            Material mat = filter.get(i);
            setItem(i, createItem(mat, "&c" + mat.name(), "&7Нажмите для удаления"));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (event.getClickedInventory() == inventory) {
            // Clicked top inventory -> Remove
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                Material type = item.getType();
                List<Material> filter = new ArrayList<>(plugin.getCooldownManager().getFilter(player));
                filter.remove(type);
                plugin.getCooldownManager().setFilter(player, filter);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                loadFilter();
            }
        } else {
            // Clicked player inventory -> Add
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                Material type = item.getType();
                List<Material> filter = new ArrayList<>(plugin.getCooldownManager().getFilter(player));
                if (!filter.contains(type)) {
                    filter.add(type);
                    plugin.getCooldownManager().setFilter(player, filter);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    loadFilter();
                } else {
                    player.sendMessage(ChatUtil.color("&cЭтот предмет уже в фильтре!"));
                }
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(name));
            if (lore.length > 0) {
                meta.setLore(ChatUtil.color(Arrays.asList(lore)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
