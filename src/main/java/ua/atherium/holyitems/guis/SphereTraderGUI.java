package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Arrays;

public class SphereTraderGUI extends BaseGUI {

    public SphereTraderGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Сферомант");
        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        setItem(11, createItem(Material.EMERALD, "&aКупить сферы", "&7Обмен осколков на сферы"));
        setItem(13, createItem(Material.ANVIL, "&eУлучшить сферы", "&7Комбинирование сфер"));
        setItem(15, createItem(Material.TOTEM_OF_UNDYING, "&dСоздать талисман", "&7Превращение сферы в талисман"));

        setItem(26, createItem(Material.ARROW, "&cНазад"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 11) new SphereShopGUI(plugin, player).open();
        if (slot == 13) new SphereUpgradeGUI(plugin, player).open();
        if (slot == 15) new SphereConverterGUI(plugin, player).open();
        if (slot == 26) new MainMenuGUI(plugin, player).open();
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
