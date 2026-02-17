package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.Sphere;
import ua.atherium.holyitems.objects.SphereType;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Arrays;

public class SphereConverterGUI extends BaseGUI {

    private final ItemStack confirmButton;
    private final ItemStack backButton;
    private Sphere selectedSphere = null;

    public SphereConverterGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Конвертация Сфер");

        confirmButton = createItem(Material.LIME_STAINED_GLASS_PANE, "&aПодтвердить", "&7Нажмите для превращения");
        backButton = createItem(Material.ARROW, "&cНазад");

        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        setItem(11, new ItemStack(Material.AIR)); // Input slot
        setItem(13, createItem(Material.IRON_BARS, "&7->"));
        setItem(15, createItem(Material.BARRIER, "&cРезультат")); // Output preview

        setItem(22, confirmButton);
        setItem(26, backButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (event.getClickedInventory() == inventory) {
            if (slot == 26) {
                returnItem();
                new SphereTraderGUI(plugin, player).open();
                return;
            }

            if (slot == 22) {
                processConversion();
                return;
            }

            if (slot == 11) {
                event.setCancelled(false);
                plugin.getServer().getScheduler().runTask(plugin, this::updateState);
            }
        } else {
            if (event.isShiftClick()) {
                ItemStack item = event.getCurrentItem();
                if (plugin.getSphereManager().getSphere(item) != null) {
                    if (inventory.getItem(11) == null || inventory.getItem(11).getType() == Material.AIR) {
                        inventory.setItem(11, item.clone());
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        updateState();
                    }
                }
            }
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        returnItem();
    }

    private void returnItem() {
        ItemStack input = inventory.getItem(11);
        if (input != null && input.getType() != Material.AIR) {
            player.getInventory().addItem(input).values().forEach(i -> player.getWorld().dropItem(player.getLocation(), i));
            inventory.setItem(11, new ItemStack(Material.AIR));
        }
    }

    private void updateState() {
        ItemStack input = inventory.getItem(11);
        selectedSphere = plugin.getSphereManager().getSphere(input);

        if (selectedSphere != null && selectedSphere.isConvertible() && selectedSphere.getType() == SphereType.SPHERE) {
            ItemStack result = plugin.getSphereManager().getTalismanItem(selectedSphere.getId());
            inventory.setItem(15, result);

            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                meta.setLore(ChatUtil.color(Arrays.asList(
                    "&7Стоимость: &e" + selectedSphere.getConvertCost() + " уровней",
                    "&7Нажмите для превращения"
                )));
                confirmButton.setItemMeta(meta);
            }
            inventory.setItem(22, confirmButton);
        } else {
            inventory.setItem(15, createItem(Material.BARRIER, "&cРезультат"));

            ItemMeta meta = confirmButton.getItemMeta();
             if (meta != null) {
                meta.setLore(ChatUtil.color(Arrays.asList("&cПоложите сферу в слот")));
                confirmButton.setItemMeta(meta);
             }
            inventory.setItem(22, confirmButton);
        }
    }

    private void processConversion() {
        if (selectedSphere == null) return;

        if (player.getLevel() < selectedSphere.getConvertCost()) {
            player.sendMessage(ChatUtil.color("&cНедостаточно уровней опыта!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        player.setLevel(player.getLevel() - selectedSphere.getConvertCost());

        ItemStack talisman = plugin.getSphereManager().getTalismanItem(selectedSphere.getId());

        inventory.setItem(11, new ItemStack(Material.AIR));
        selectedSphere = null;

        player.getInventory().addItem(talisman).values().forEach(i -> player.getWorld().dropItem(player.getLocation(), i));

        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
        player.sendMessage(ChatUtil.color("&aСфера успешно превращена в талисман!"));

        updateState();
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
