package ua.atherium.holyitems.guis;

import org.bukkit.Bukkit;
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

public class ShopGUI extends BaseGUI {

    private final String category;
    private final List<ItemStack> shopItems = new ArrayList<>();
    private final List<Double> prices = new ArrayList<>();

    public ShopGUI(HolyWorldItems plugin, Player player, String category) {
        super(plugin, player, 54, Utils.color("&8Магазин: " + category));
        this.category = category;
        loadItems();
    }

    private void loadItems() {
        // Iterate Items
        Map<String, ItemStack> items = plugin.getItemManager().getItems();
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            String type = plugin.getConfigManager().getConfig("items.yml").getString("items." + entry.getKey() + ".type", "OTHER");
            List<String> obtain = plugin.getConfigManager().getConfig("items.yml").getStringList("items." + entry.getKey() + ".obtain");
            double price = plugin.getConfigManager().getConfig("items.yml").getDouble("items." + entry.getKey() + ".price", -1);

            if (price > 0 && obtain.contains("/shop") && category.equalsIgnoreCase(type)) {
                ItemStack item = entry.getValue().clone();
                addPriceLore(item, price);
                shopItems.add(item);
                prices.add(price);
            }
        }

        // Iterate Potions
        if ("POTIONS".equalsIgnoreCase(category)) {
            Map<String, ItemStack> potions = plugin.getPotionManager().getPotions();
            for (Map.Entry<String, ItemStack> entry : potions.entrySet()) {
                double price = plugin.getConfigManager().getConfig("potions.yml").getDouble("potions." + entry.getKey() + ".price", -1);
                if (price > 0) {
                    ItemStack item = entry.getValue().clone();
                    addPriceLore(item, price);
                    shopItems.add(item);
                    prices.add(price);
                }
            }
        }

        // Iterate Enchantments (Books)
        if ("ENCHANTMENTS".equalsIgnoreCase(category)) {
            Map<String, ua.atherium.holyitems.managers.EnchantmentManager.CustomEnchantment> enchants = plugin.getEnchantmentManager().getEnchantments();
            for (Map.Entry<String, ua.atherium.holyitems.managers.EnchantmentManager.CustomEnchantment> entry : enchants.entrySet()) {
                ua.atherium.holyitems.managers.EnchantmentManager.CustomEnchantment ench = entry.getValue();
                // Check if obtain via shop
                // ConfigurationSection in CustomEnchantment holds config
                List<String> obtain = ench.config.getStringList("obtain");
                if (obtain.contains("/shop")) {
                    double price = ench.config.getDouble("price", 1000); // Default price?

                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = book.getItemMeta();
                    meta.setDisplayName(Utils.color("&eКнига: " + ench.name));
                    book.setItemMeta(meta);

                    // Add enchant to book
                    plugin.getEnchantmentManager().addEnchantment(book, ench.id, 1); // Level 1 book

                    addPriceLore(book, price);
                    shopItems.add(book);
                    prices.add(price);
                }
            }
        }

        // Populate Inventory
        for (int i = 0; i < shopItems.size() && i < 54; i++) {
            inventory.setItem(i, shopItems.get(i));
        }

        // Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(Utils.color("&cНазад"));
        back.setItemMeta(meta);
        inventory.setItem(53, back);
    }

    private void addPriceLore(ItemStack item, double price) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(Utils.color(""));
        lore.add(Utils.color("&7Цена: &a" + price + " монет"));
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

        if (slot >= 0 && slot < shopItems.size()) {
            double price = prices.get(slot);
            if (plugin.getEconomyManager().hasMoney(player.getName(), price)) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(Utils.color("&cИнвентарь полон!"));
                    return;
                }

                plugin.getEconomyManager().withdraw(player.getName(), price);

                // Get original item without price lore? Or just give it?
                // Better give clean item.
                // Re-fetch clean item or clone shop item and remove last lore lines.
                // I'll fetch clean item by ID?
                // But shopItems stores clones.
                // Easier to remove lore lines I added.
                ItemStack bought = shopItems.get(slot).clone();
                ItemMeta meta = bought.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null && lore.size() >= 3) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1); // remove 3 lines added
                    meta.setLore(lore);
                    bought.setItemMeta(meta);
                }

                player.getInventory().addItem(bought);
                player.sendMessage(Utils.color("&aВы купили предмет за " + price + " монет."));
            } else {
                player.sendMessage(Utils.color("&cНедостаточно средств!"));
            }
        }
    }
}
