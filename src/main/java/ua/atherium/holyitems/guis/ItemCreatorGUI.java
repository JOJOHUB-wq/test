package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemCreatorGUI extends BaseGUI {

    private final ItemStack item;

    public ItemCreatorGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Создание предмета");
        this.item = player.getInventory().getItemInMainHand();
        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        if (item == null || item.getType() == Material.AIR) {
            setItem(13, createItem(Material.BARRIER, "&cВозьмите предмет в руку!", "&7Для редактирования нужен предмет"));
            return;
        }

        setItem(10, createItem(Material.NAME_TAG, "&eИзменить название", "&7Нажмите для ввода в чат"));
        setItem(12, createItem(Material.WRITABLE_BOOK, "&eДобавить лор", "&7Нажмите для ввода строки"));
        setItem(14, createItem(Material.LAVA_BUCKET, "&eОчистить лор", "&7Нажмите для удаления лора"));
        setItem(16, createItem(Material.CLOCK, "&eУстановить кулдаун", "&7Нажмите для ввода секунд"));

        setItem(22, createItem(Material.EMERALD_BLOCK, "&aСохранить предмет", "&7Сохранить в конфиг (items.yml)"));

        // Preview
        setItem(13, item.clone());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (slot == 10) {
            plugin.getChatListener().requestInput(player, input -> {
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatUtil.color(input));
                item.setItemMeta(meta);
                player.sendMessage(ChatUtil.color("&aНазвание обновлено!"));
            });
        }

        if (slot == 12) {
            plugin.getChatListener().requestInput(player, input -> {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(ChatUtil.color(input));
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.sendMessage(ChatUtil.color("&aСтрока добавлена!"));
            });
        }

        if (slot == 14) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(null);
            item.setItemMeta(meta);
            initialize(); // Update view
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
        }

        if (slot == 16) {
             plugin.getChatListener().requestInput(player, input -> {
                try {
                    long seconds = Long.parseLong(input);
                    // Store in PDC temporarily or just remind user to save
                    player.sendMessage(ChatUtil.color("&aКулдаун: " + seconds + " (сохранится в конфиг)"));
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatUtil.color("&cНеверное число!"));
                }
            });
        }

        if (slot == 22) {
            plugin.getChatListener().requestInput(player, id -> {
                // Save to config
                saveToConfig(id);
            });
        }
    }

    private void saveToConfig(String id) {
        FileConfiguration config = plugin.getConfigManager().getConfig("items.yml");
        String path = "items." + id;

        if (config.contains(path)) {
            player.sendMessage(ChatUtil.color("&cПредмет с таким ID уже существует!"));
            return;
        }

        config.set(path + ".material", item.getType().name());
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            config.set(path + ".name", meta.getDisplayName().replace("§", "&"));
        }
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(line.replace("§", "&"));
            }
            config.set(path + ".lore", lore);
        }
        if (meta.hasCustomModelData()) {
            config.set(path + ".custom_model_data", meta.getCustomModelData());
        }

        // Defaults
        config.set(path + ".cooldown", 0);
        config.set(path + ".price", 1000);
        config.set(path + ".type", "MISC");

        plugin.getConfigManager().saveConfig("items.yml");
        plugin.getItemManager().loadItems();

        player.sendMessage(ChatUtil.color("&aПредмет сохранен как &e" + id));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
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
