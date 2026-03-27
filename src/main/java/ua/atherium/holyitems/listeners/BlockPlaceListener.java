package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

public class BlockPlaceListener implements Listener {

    private final HolyWorldItems plugin;

    public BlockPlaceListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        String id = plugin.getItemManager().getItemId(item);

        if (id == null) return;

        // Region Check (already handled by WorldGuard usually, but custom check requested for traps)
        // For machines, we rely on standard protection.

        switch (id) {
            case "fast_furnace":
                plugin.getMachineManager().addMachine(event.getBlock().getLocation(), "fast_furnace");
                player.sendMessage(Utils.color("&aБыстрая печка установлена!"));
                break;
            case "golden_spawner":
                plugin.getMachineManager().addMachine(event.getBlock().getLocation(), "golden_spawner");
                player.sendMessage(Utils.color("&aЗолотой спавнер установлен!"));
                if (plugin.getConfigManager().getConfig("items.yml").getBoolean("items.golden_spawner.broadcast_coords")) {
                     // Broadcast? Prompt says "Chat Broadcast: Shows coordinates on place"
                     plugin.getServer().broadcastMessage(Utils.color("&6Игрок " + player.getName() + " установил Золотой спавнер на " +
                         event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ() + "!"));
                }
                break;
            case "auto_crafter":
                plugin.getMachineManager().addMachine(event.getBlock().getLocation(), "auto_crafter");
                player.sendMessage(Utils.color("&aАвто-крафтер установлен!"));
                break;
        }
    }
}
