package com.customenchants.menu;

import com.customenchants.CustomEnchants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class MenuListener implements Listener {

    private final CustomEnchants plugin;

    public MenuListener(CustomEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof Menu)) {
            return;
        }

        event.setCancelled(true);

        Menu menu = (Menu) holder;
        String itemKey = menu.getItemKey(event.getSlot());

        if (itemKey == null) {
            return;
        }

        FileConfiguration menuConfig = plugin.getMenuManager().getMenuConfig(menu.getId());
        if (menuConfig == null) {
            return;
        }

        String clickTypePath = event.isLeftClick() ? "on_left_click" : "on_right_click";
        List<String> commands = menuConfig.getStringList("items." + itemKey + "." + clickTypePath + ".commands");

        if (commands.isEmpty()) {
            return;
        }

        for (String command : commands) {
            processCommand(player, command);
        }
    }

    private void processCommand(Player player, String command) {
        if (command.startsWith("[OPEN]")) {
            String menuId = command.substring(7);
            plugin.getMenuManager().openMenu(player, menuId);
        } else if (command.equalsIgnoreCase("[CLOSE]")) {
            player.closeInventory();
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }
}