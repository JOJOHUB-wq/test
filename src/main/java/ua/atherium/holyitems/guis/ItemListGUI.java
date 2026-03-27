package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.*;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemListGUI extends BaseGUI {

    private final String category;
    private final List<Object> objects = new ArrayList<>();

    public ItemListGUI(HolyWorldItems plugin, Player player, String category) {
        super(plugin, player, 54, "&8" + category);
        this.category = category;
        loadItems();
    }

    private void loadItems() {
        if ("SPHERE".equals(category)) {
            objects.addAll(plugin.getSphereManager().getAllSpheres());
        } else if ("ENCHANTMENT".equals(category)) {
             objects.addAll(plugin.getEnchantmentManager().getAllEnchantments());
        } else if ("POTION".equals(category)) {
             objects.addAll(plugin.getPotionManager().getAllPotions());
        } else {
             for (CustomItem item : plugin.getItemManager().getAllItems()) {
                 if (item.getType().equals(category)) {
                     objects.add(item);
                 }
             }
        }

        for (int i = 0; i < objects.size() && i < 45; i++) {
            Object obj = objects.get(i);
            ItemStack stack = null;

            if (obj instanceof CustomItem) {
                stack = ((CustomItem) obj).getItemStack();
            } else if (obj instanceof Sphere) {
                stack = ((Sphere) obj).getItemStack();
            } else if (obj instanceof CustomPotion) {
                stack = plugin.getPotionManager().getPotionItem(((CustomPotion) obj).getId());
            } else if (obj instanceof CustomEnchantment) {
                CustomEnchantment ench = (CustomEnchantment) obj;
                stack = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatUtil.color(ench.getName()));
                    meta.setLore(ChatUtil.color(Arrays.asList("&7" + ench.getDescription(), "", "&eLevel: " + ench.getMaxLevel())));
                    stack.setItemMeta(meta);
                }
            }

            if (stack != null) {
                setItem(i, stack);
            }
        }

        fillBorder(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        setItem(49, createItem(Material.ARROW, "&cНазад"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 49) {
            new MainMenuGUI(plugin, player).open();
            return;
        }

        if (slot >= 0 && slot < objects.size()) {
            Object obj = objects.get(slot);
            if (player.hasPermission("holyitems.admin")) {
                // Give item
                if (obj instanceof CustomItem) {
                    plugin.getItemManager().giveItem(player, ((CustomItem) obj).getId(), 1);
                } else if (obj instanceof Sphere) {
                    player.getInventory().addItem(((Sphere) obj).getItemStack());
                } else if (obj instanceof CustomPotion) {
                    player.getInventory().addItem(plugin.getPotionManager().getPotionItem(((CustomPotion) obj).getId()));
                } else if (obj instanceof CustomEnchantment) {
                    // Give book? Or apply to item in hand?
                    // Just give info or book
                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = book.getItemMeta();
                    meta.setDisplayName(ChatUtil.color(((CustomEnchantment) obj).getName()));
                    book.setItemMeta(meta);
                    player.getInventory().addItem(book);
                }
                player.sendMessage(ChatUtil.color("&aВыдан предмет!"));
            } else {
                // Open Info GUI? Or just view.
                // For now just do nothing or play sound.
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtil.color(name));
            meta.setLore(ChatUtil.color(Arrays.asList(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }
}
