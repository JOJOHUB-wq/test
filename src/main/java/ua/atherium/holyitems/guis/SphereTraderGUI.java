package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SphereTraderGUI extends BaseGUI {

    private final List<ItemStack> spheres = new ArrayList<>();
    private final List<Integer> costs = new ArrayList<>();

    public SphereTraderGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 54, Utils.color("&8Сферомант"));
        loadSpheres();
    }

    private void loadSpheres() {
        Map<String, ItemStack> sphereMap = plugin.getSphereManager().getSpheres();
        for (Map.Entry<String, ItemStack> entry : sphereMap.entrySet()) {
            ItemStack item = entry.getValue().clone();
            // Price?
            // "Common: 30 levels"? No, that's convert cost.
            // "Trade with 'Сферомант'... Amount depends on rarity".
            // Prompt doesn't specify BUY price in shards.
            // "Smelt sphere -> shards. Trade with 'Сферомант'".
            // Usually trade shards -> spheres.
            // Let's assume price based on rarity.
            // Common: 10 shards. Epic: 20. Legendary: 40. Unique: 100.
            // Or config.
            // I'll use hardcoded for now or check config "shard_price".
            int price = 10;
            String rarity = item.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getRarityKey(), org.bukkit.persistence.PersistentDataType.STRING);
            if ("EPIC".equals(rarity)) price = 20;
            if ("LEGENDARY".equals(rarity)) price = 40;
            if ("UNIQUE".equals(rarity)) price = 100;

            addPriceLore(item, price);
            spheres.add(item);
            costs.add(price);
        }

        for (int i = 0; i < spheres.size() && i < 45; i++) {
            inventory.setItem(i, spheres.get(i));
        }

        // Buttons
        setItem(49, Material.ANVIL, "&aУлучшение сфер", "UPGRADE");
        setItem(50, Material.TOTEM_OF_UNDYING, "&bКонвертация в талисман", "CONVERT");
        setItem(53, Material.ARROW, "&cНазад", "BACK");
    }

    private void setItem(int slot, Material mat, String name, String tag) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.color(name));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void addPriceLore(ItemStack item, int price) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(Utils.color(""));
        lore.add(Utils.color("&7Цена: &b" + price + " осколков"));
        lore.add(Utils.color("&eНажмите, чтобы купить"));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == 53) {
            new MainGUI(plugin, player).open();
            return;
        }
        if (slot == 49) {
            // Open Upgrade GUI (Anvil-like? Or custom)
            // Since anvil listener handles combining, maybe just tell player?
            // "Upgrade: Combine 2 same rarity spheres in anvil".
            // So this button is info? Or distinct system?
            // "Upgrade GUI (combine spheres)".
            // Let's implement a custom GUI for combining if anvil is tricky.
            // But AnvilListener is already done.
            // I'll make this button show info.
            player.sendMessage(Utils.color("&eДля улучшения объедините две одинаковые сферы в наковальне!"));
            player.closeInventory();
            return;
        }
        if (slot == 50) {
            new SphereConverterGUI(plugin, player).open();
            return;
        }

        if (slot >= 0 && slot < spheres.size()) {
            int price = costs.get(slot);
            if (hasShards(player, price)) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(Utils.color("&cИнвентарь полон!"));
                    return;
                }

                removeShards(player, price);

                ItemStack bought = spheres.get(slot).clone();
                ItemMeta meta = bought.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null && lore.size() >= 3) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                    meta.setLore(lore);
                    bought.setItemMeta(meta);
                }

                player.getInventory().addItem(bought);
                player.sendMessage(Utils.color("&aВы купили сферу за " + price + " осколков."));
            } else {
                player.sendMessage(Utils.color("&cНедостаточно осколков!"));
            }
        }
    }

    private boolean hasShards(Player player, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PRISMARINE_SHARD) {
                // Check name? "Осколок сферы".
                // Simple check for material + name match?
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().contains("Осколок сферы")) {
                    count += item.getAmount();
                }
            }
        }
        return count >= amount;
    }

    private void removeShards(Player player, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PRISMARINE_SHARD) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().contains("Осколок сферы")) {
                    if (item.getAmount() <= remaining) {
                        remaining -= item.getAmount();
                        item.setAmount(0);
                    } else {
                        item.setAmount(item.getAmount() - remaining);
                        remaining = 0;
                    }
                    if (remaining <= 0) break;
                }
            }
        }
    }
}
