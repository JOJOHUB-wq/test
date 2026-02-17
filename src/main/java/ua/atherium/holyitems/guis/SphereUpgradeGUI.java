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
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Arrays;

public class SphereUpgradeGUI extends BaseGUI {

    private final ItemStack confirmButton;
    private final ItemStack backButton;
    private Sphere resultSphere = null;

    public SphereUpgradeGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Улучшение Сфер");

        confirmButton = createItem(Material.LIME_STAINED_GLASS_PANE, "&aУлучшить", "&7Нажмите для объединения");
        backButton = createItem(Material.ARROW, "&cНазад");

        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        setItem(10, new ItemStack(Material.AIR)); // Input 1
        setItem(12, new ItemStack(Material.AIR)); // Input 2
        setItem(11, createItem(Material.IRON_BARS, "&7+"));
        setItem(14, createItem(Material.IRON_BARS, "&7->"));
        setItem(16, createItem(Material.BARRIER, "&cРезультат")); // Output

        setItem(22, confirmButton);
        setItem(26, backButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (event.getClickedInventory() == inventory) {
            if (slot == 26) {
                returnItems();
                new SphereTraderGUI(plugin, player).open();
                return;
            }

            if (slot == 22) {
                processUpgrade();
                return;
            }

            if (slot == 10 || slot == 12) {
                event.setCancelled(false);
                plugin.getServer().getScheduler().runTask(plugin, this::updateState);
            }
        } else {
            if (event.isShiftClick()) {
                ItemStack item = event.getCurrentItem();
                if (plugin.getSphereManager().getSphere(item) != null) {
                    if (inventory.getItem(10) == null || inventory.getItem(10).getType() == Material.AIR) {
                        inventory.setItem(10, item.clone());
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        updateState();
                    } else if (inventory.getItem(12) == null || inventory.getItem(12).getType() == Material.AIR) {
                        inventory.setItem(12, item.clone());
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        updateState();
                    }
                }
            }
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        returnItems();
    }

    private void returnItems() {
        returnItem(10);
        returnItem(12);
    }

    private void returnItem(int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getType() != Material.AIR) {
            player.getInventory().addItem(item).values().forEach(i -> player.getWorld().dropItem(player.getLocation(), i));
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    private void updateState() {
        ItemStack item1 = inventory.getItem(10);
        ItemStack item2 = inventory.getItem(12);

        Sphere s1 = plugin.getSphereManager().getSphere(item1);
        Sphere s2 = plugin.getSphereManager().getSphere(item2);

        resultSphere = null;

        if (s1 != null && s2 != null && s1.getId().equals(s2.getId())) {
            // Check for upgrade: common_damage_1 -> epic_damage_2
            String nextId = getNextTierId(s1.getId());
            if (nextId != null) {
                Sphere next = plugin.getSphereManager().getSphere(nextId);
                if (next != null) {
                    resultSphere = next;
                }
            }
        }

        if (resultSphere != null) {
            inventory.setItem(16, resultSphere.getItemStack());

            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                meta.setLore(ChatUtil.color(Arrays.asList("&aДоступно улучшение!", "&7" + s1.getRarity() + " -> " + resultSphere.getRarity())));
                confirmButton.setItemMeta(meta);
            }
            inventory.setItem(22, confirmButton);
        } else {
            inventory.setItem(16, createItem(Material.BARRIER, "&cРезультат"));

            ItemMeta meta = confirmButton.getItemMeta();
             if (meta != null) {
                meta.setLore(ChatUtil.color(Arrays.asList("&cНужны 2 одинаковые сферы", "&cдля улучшения")));
                confirmButton.setItemMeta(meta);
             }
            inventory.setItem(22, confirmButton);
        }
    }

    private String getNextTierId(String id) {
        if (id.startsWith("common_") && id.endsWith("_1")) {
            return id.replace("common_", "epic_").replace("_1", "_2");
        }
        if (id.startsWith("epic_") && id.endsWith("_2")) {
            return id.replace("epic_", "legendary_").replace("_2", "_3");
        }
        return null;
    }

    private void processUpgrade() {
        if (resultSphere == null) return;

        inventory.setItem(10, new ItemStack(Material.AIR));
        inventory.setItem(12, new ItemStack(Material.AIR));

        player.getInventory().addItem(resultSphere.getItemStack()).values().forEach(i -> player.getWorld().dropItem(player.getLocation(), i));

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
        player.sendMessage(ChatUtil.color("&aСфера успешно улучшена!"));

        resultSphere = null;
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
