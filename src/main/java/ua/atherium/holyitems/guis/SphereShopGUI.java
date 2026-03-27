package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.Sphere;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SphereShopGUI extends BaseGUI {

    private final List<Sphere> spheres = new ArrayList<>();

    public SphereShopGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 54, "&8Магазин Сфер");
        loadItems();
    }

    private void loadItems() {
        spheres.addAll(plugin.getSphereManager().getAllSpheres());

        for (int i = 0; i < spheres.size() && i < 45; i++) {
            Sphere sphere = spheres.get(i);
            int cost = sphere.getShardValue() * 2;

            ItemStack icon = sphere.getItemStack();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatUtil.color("&7Цена: &b" + cost + " осколков"));
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }

            setItem(i, icon);
        }

        fillBorder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        setItem(49, createItem(Material.ARROW, "&cНазад"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 49) {
            new SphereTraderGUI(plugin, player).open();
            return;
        }

        if (slot >= 0 && slot < spheres.size()) {
            Sphere sphere = spheres.get(slot);
            int cost = sphere.getShardValue() * 2;

            if (hasShards(player, cost)) {
                removeShards(player, cost);
                player.getInventory().addItem(sphere.getItemStack());
                player.sendMessage(ChatUtil.color("&aВы купили сферу!"));
            } else {
                player.sendMessage(ChatUtil.color("&cНедостаточно осколков!"));
            }
        }
    }

    private boolean hasShards(Player player, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getSphereManager().isShard(item)) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void removeShards(Player player, int amount) {
        int left = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.getSphereManager().isShard(item)) {
                if (item.getAmount() <= left) {
                    left -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - left);
                    left = 0;
                }
                if (left <= 0) break;
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
