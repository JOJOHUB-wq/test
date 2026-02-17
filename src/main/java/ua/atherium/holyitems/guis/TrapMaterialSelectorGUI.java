package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Arrays;
import java.util.List;

public class TrapMaterialSelectorGUI extends BaseGUI {

    private static final List<Material> MATERIALS = Arrays.asList(
            Material.OBSIDIAN, Material.COBWEB, Material.GLASS, Material.STONE,
            Material.DIRT, Material.OAK_PLANKS, Material.NETHERRACK, Material.BEDROCK
    );

    public TrapMaterialSelectorGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, "&8Выбор материала трапки");
        initialize();
    }

    private void initialize() {
        fillBorder(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        for (int i = 0; i < MATERIALS.size(); i++) {
            Material mat = MATERIALS.get(i);
            setItem(10 + i, createItem(mat, "&e" + mat.name(), "&7Нажмите для выбора"));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Material type = clicked.getType();
        if (MATERIALS.contains(type)) {
            // Save to player data
            // We use CooldownManager as PlayerDataManager for now
            // But I didn't add setTrapMaterial to CooldownManager yet.
            // I'll add it now.
            plugin.getCooldownManager().setTrapMaterial(player, type);
            player.sendMessage(ChatUtil.color("&aМатериал трапки установлен: &e" + type.name()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.closeInventory();
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
