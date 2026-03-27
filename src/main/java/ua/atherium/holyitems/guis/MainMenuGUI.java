package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Arrays;

public class MainMenuGUI extends BaseGUI {

    public MainMenuGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Меню HolyWorldItems");
        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        setItem(10, createItem(Material.DIAMOND_SWORD, "&cБоевые предметы", "&7Нажмите для просмотра"));
        setItem(11, createItem(Material.COBWEB, "&7Ловушки", "&7Нажмите для просмотра"));
        setItem(12, createItem(Material.CHEST, "&eУтилитарные предметы", "&7Нажмите для просмотра"));
        setItem(13, createItem(Material.MAGMA_CREAM, "&dСферы и Талисманы", "&7Нажмите для просмотра"));
        setItem(14, createItem(Material.ENCHANTED_BOOK, "&bЗачарования", "&7Нажмите для просмотра"));
        setItem(15, createItem(Material.POTION, "&5Зелья", "&7Нажмите для просмотра"));
        setItem(16, createItem(Material.EMERALD, "&aТорговец сферами", "&7Нажмите для перехода"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 10) new ItemListGUI(plugin, player, "COMBAT").open();
        if (slot == 11) new ItemListGUI(plugin, player, "TRAP").open();
        if (slot == 12) new ItemListGUI(plugin, player, "UTILITY").open();
        if (slot == 13) new ItemListGUI(plugin, player, "SPHERE").open();
        if (slot == 14) new ItemListGUI(plugin, player, "ENCHANTMENT").open();
        if (slot == 15) new ItemListGUI(plugin, player, "POTION").open();
        if (slot == 16) new SphereTraderGUI(plugin, player).open();
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
