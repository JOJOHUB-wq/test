package ua.atherium.holyitems.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class CustomMaterialGUI extends BaseGUI {

    private static final List<Material> MATERIALS = Arrays.asList(
            Material.COBWEB, Material.OBSIDIAN, Material.DIRT, Material.STONE,
            Material.OAK_PLANKS, Material.GLASS, Material.TNT, Material.BEDROCK
    );

    public CustomMaterialGUI(HolyWorldItems plugin, Player player) {
        super(plugin, player, 27, Utils.color("&8Выбор материала ловушки"));
        init();
    }

    private void init() {
        int i = 0;
        for (Material mat : MATERIALS) {
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.color("&e" + mat.name()));
            meta.setLore(Arrays.asList(Utils.color("&7Нажмите для выбора")));
            item.setItemMeta(meta);
            inventory.setItem(i++, item);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        Material mat = event.getCurrentItem().getType();
        if (MATERIALS.contains(mat)) {
            plugin.getPlayerDataManager().setTrapMaterial(player, mat);
            player.sendMessage(Utils.color("&aМатериал ловушки установлен: " + mat.name()));
            player.closeInventory();
        }
    }
}
