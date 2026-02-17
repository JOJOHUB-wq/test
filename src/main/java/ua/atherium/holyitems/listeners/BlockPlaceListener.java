package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.managers.ItemManager;

public class BlockPlaceListener implements Listener {

    private final HolyWorldItems plugin;
    private final ItemManager itemManager;

    public BlockPlaceListener(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getItemManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();

        if (!item.hasItemMeta()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        // Check if custom item
        if (!pdc.has(plugin.getItemManager().getIdKey(), PersistentDataType.STRING)) {
            return;
        }

        String itemId = pdc.get(plugin.getItemManager().getIdKey(), PersistentDataType.STRING);

        // Handle specific placeable items
        switch (itemId) {
            case "fast_furnace":
                handleFastFurnace(block, player);
                break;

            case "golden_spawner":
                handleGoldenSpawner(block, player);
                break;

            case "auto_crafter":
                handleAutoCrafter(block, player);
                break;

            default:
                break;
        }
    }

    private void handleFastFurnace(Block block, Player player) {
        if (block.getType() != Material.FURNACE) return;

        // Store custom data in block
        // In reality, TileState is needed to store PDC on block
        if (block.getState() instanceof org.bukkit.block.TileState) {
            org.bukkit.block.TileState state = (org.bukkit.block.TileState) block.getState();
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(plugin.getKey("fast_furnace"), PersistentDataType.BYTE, (byte) 1);
            pdc.set(plugin.getKey("speed_multiplier"), PersistentDataType.DOUBLE, 3.0);
            pdc.set(plugin.getKey("fuel_efficiency"), PersistentDataType.DOUBLE, 1.5);
            state.update();
        }

        player.sendMessage(plugin.getMessage("fast-furnace-placed"));
    }

    private void handleGoldenSpawner(Block block, Player player) {
        if (block.getType() != Material.SPAWNER) return;

        if (block.getState() instanceof org.bukkit.block.TileState) {
            org.bukkit.block.TileState state = (org.bukkit.block.TileState) block.getState();
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(plugin.getKey("golden_spawner"), PersistentDataType.BYTE, (byte) 1);
            pdc.set(plugin.getKey("durability"), PersistentDataType.INTEGER, 100);
            pdc.set(plugin.getKey("last_generate"), PersistentDataType.LONG, System.currentTimeMillis());
            state.update();
        }

        // Broadcast coordinates
        String message = plugin.getMessage("golden-spawner-placed")
            .replace("{player}", player.getName())
            .replace("{x}", String.valueOf(block.getX()))
            .replace("{y}", String.valueOf(block.getY()))
            .replace("{z}", String.valueOf(block.getZ()));

        plugin.getServer().broadcastMessage(message);

        plugin.getGoldenSpawners().add(block.getLocation());
    }

    private void handleAutoCrafter(Block block, Player player) {
        if (block.getType() != Material.DISPENSER) return;

        if (block.getState() instanceof org.bukkit.block.TileState) {
            org.bukkit.block.TileState state = (org.bukkit.block.TileState) block.getState();
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(plugin.getKey("auto_crafter"), PersistentDataType.BYTE, (byte) 1);
            state.update();
        }

        player.sendMessage(plugin.getMessage("auto-crafter-placed"));
    }
}
