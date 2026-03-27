package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.Arrays;

public class MainGUI extends BaseGUI {

    public MainGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, Utils.color("&8HolyWorld Items"));
        init();
    }

    private void init() {
        // Categories
        setItem(10, Material.DIAMOND_SWORD, "&cБоевые предметы", "COMBAT");
        setItem(11, Material.COBWEB, "&7Ловушки", "TRAP");
        setItem(12, Material.GOLDEN_PICKAXE, "&eУтилитарные", "UTILITY");
        setItem(13, Material.MAGMA_CREAM, "&bСферы и Талисманы", "SPHERES");
        setItem(14, Material.ENCHANTED_BOOK, "&dЗачарования", "ENCHANTMENTS");
        setItem(15, Material.POTION, "&5Зелья", "POTIONS");
        setItem(16, Material.EMERALD, "&aМагазин осколков", "SHARD_SHOP"); // Redirect to Sphere Trader
    }

    private void setItem(int slot, Material mat, String name, String category) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.color(name));
        meta.setLore(Arrays.asList(Utils.color("&7Нажмите, чтобы открыть категорию")));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == 10) new ShopGUI(plugin, player, "COMBAT").open();
        else if (slot == 11) new ShopGUI(plugin, player, "TRAP").open();
        else if (slot == 12) new ShopGUI(plugin, player, "UTILITY").open();
        else if (slot == 13) new ShopGUI(plugin, player, "SPHERES").open(); // Or SphereTrader?
        // Sphere category shop usually sells BASE spheres? Or random?
        // Prompt: "Obtain: /shop". "Price: ..."
        // So ShopGUI handles Spheres too.
        else if (slot == 14) new ShopGUI(plugin, player, "ENCHANTMENTS").open();
        else if (slot == 15) new ShopGUI(plugin, player, "POTIONS").open();
        else if (slot == 16) new SphereTraderGUI(plugin, player).open();
    }
}
