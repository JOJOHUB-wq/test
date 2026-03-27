package ua.atherium.holyitems.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ua.atherium.holyitems.HolyWorldItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {

    private final HolyWorldItems plugin;
    private final Map<UUID, Inventory> openGUIs = new HashMap<>();

    public GUIManager(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        new ua.atherium.holyitems.guis.MainGUI(plugin, player).open();
    }

    public void openSphereTrader(Player player) {
        new ua.atherium.holyitems.guis.SphereTraderGUI(plugin, player).open();
    }

    public void openCreator(Player player) {
        new ua.atherium.holyitems.guis.ItemCreatorGUI(plugin, player).open();
    }

    public void openCustomMaterialSelector(Player player) {
        new ua.atherium.holyitems.guis.CustomMaterialGUI(plugin, player).open();
    }
}
