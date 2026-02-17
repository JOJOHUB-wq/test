package ua.atherium.holyitems.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.*;

public class BlockBreakListener implements Listener {

    private final HolyWorldItems plugin;
    private final Set<Location> processing = new HashSet<>();

    public BlockBreakListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (processing.contains(event.getBlock().getLocation())) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) return;

        // Safeguard
        if (plugin.getEnchantmentManager().hasEnchant(tool, "SAFEGUARD")) {
            if (tool.getItemMeta() instanceof Damageable) {
                Damageable meta = (Damageable) tool.getItemMeta();
                int max = tool.getType().getMaxDurability();
                if (max > 0 && (max - meta.getDamage()) <= 10) {
                    event.setCancelled(true);
                    player.sendMessage(ChatUtil.color("&cПредмет почти сломан!"));
                    return;
                }
            }
        }

        // Delicate
        if (plugin.getEnchantmentManager().hasEnchant(tool, "DELICATE")) {
            if (event.getBlock().getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) event.getBlock().getBlockData();
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Drill / Mega Drill
        if (plugin.getEnchantmentManager().hasEnchant(tool, "MEGA_DRILL")) {
            handleAreaMine(player, event.getBlock(), 5, 2, tool);
        } else if (plugin.getEnchantmentManager().hasEnchant(tool, "DRILL_2")) {
            handleAreaMine(player, event.getBlock(), 3, 2, tool);
        } else if (plugin.getEnchantmentManager().hasEnchant(tool, "DRILL_1")) {
            handleAreaMine(player, event.getBlock(), 3, 1, tool);
        }

        // Lumberjack
        if (plugin.getEnchantmentManager().hasEnchant(tool, "LUMBERJACK") && event.getBlock().getType().name().contains("LOG")) {
            handleLumberjack(player, event.getBlock(), tool);
        }

        // Handling Drops
        handleDrops(event, tool);

        // Experienced
        if (plugin.getEnchantmentManager().hasEnchant(tool, "EXPERIENCED")) {
            event.setExpToDrop((int) (event.getExpToDrop() * 1.3));
        }

        // Replant
        if (plugin.getEnchantmentManager().hasEnchant(tool, "REPLANT")) {
            if (event.getBlock().getBlockData() instanceof Ageable) {
                Material type = event.getBlock().getType();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getBlock().getType() == Material.AIR) {
                        event.getBlock().setType(type);
                    }
                }, 2L);
            }
        }
    }

    private void handleAreaMine(Player player, Block center, int size, int depth, ItemStack tool) {
        int r = size / 2;
        for (int x = -r; x <= r; x++) {
            for (int y = 0; y < depth; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block b = center.getRelative(x, y, z);
                    if (b.getType() != Material.AIR && b.getType() != Material.BEDROCK) {
                        if (plugin.getRegionManager().canBuild(player, b.getLocation())) {
                            processing.add(b.getLocation());
                            b.breakNaturally(tool);
                            processing.remove(b.getLocation());
                        }
                    }
                }
            }
        }
    }

    private void handleLumberjack(Player player, Block start, ItemStack tool) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        Set<Block> visited = new HashSet<>();
        visited.add(start);

        int limit = 128; // Max logs
        int count = 0;

        while (!queue.isEmpty() && count < limit) {
            Block current = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block relative = current.getRelative(x, y, z);
                        if (visited.contains(relative)) continue;
                        if (!relative.getType().name().contains("LOG")) continue;
                        if (!plugin.getRegionManager().canBuild(player, relative.getLocation())) continue;

                        queue.add(relative);
                        visited.add(relative);

                        if (relative != start) {
                            processing.add(relative.getLocation());
                            relative.breakNaturally(tool);
                            processing.remove(relative.getLocation());
                            count++;
                        }
                    }
                }
            }
        }
    }

    private void handleDrops(BlockBreakEvent event, ItemStack tool) {
        // We can't easily modify drops in BlockBreakEvent without clearing drops and re-dropping.
        // But if Magnetism/AutoSmelt/Filter are active, we must intervene.

        boolean magnetism = plugin.getEnchantmentManager().hasEnchant(tool, "MAGNETISM");
        boolean autoSmelt = plugin.getEnchantmentManager().hasEnchant(tool, "AUTO_SMELT");
        boolean filter = plugin.getEnchantmentManager().hasEnchant(tool, "FILTER");
        boolean farmer = plugin.getEnchantmentManager().hasEnchant(tool, "FARMER");

        if (!magnetism && !autoSmelt && !filter && !farmer) return;

        event.setDropItems(false);
        Collection<ItemStack> drops = event.getBlock().getDrops(tool);
        List<Material> filtered = filter ? plugin.getCooldownManager().getFilter(event.getPlayer()) : new ArrayList<>();

        for (ItemStack drop : drops) {
            if (filter && filtered.contains(drop.getType())) continue;

            if (autoSmelt) {
                // Simple auto-smelt map
                Material result = getSmeltResult(drop.getType());
                if (result != null) drop.setType(result);
            }

            if (farmer && isCrop(event.getBlock().getType())) {
                int level = plugin.getEnchantmentManager().getEnchantLevel(tool, "FARMER");
                drop.setAmount((int) (drop.getAmount() * (1 + (0.2 * level))));
            }

            if (magnetism) {
                HashMap<Integer, ItemStack> left = event.getPlayer().getInventory().addItem(drop);
                for (ItemStack l : left.values()) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), l);
                }
            } else {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            }
        }
    }

    private boolean isCrop(Material mat) {
        return mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.BEETROOTS || mat == Material.NETHER_WART;
    }

    private Material getSmeltResult(Material input) {
        switch (input) {
            case RAW_IRON: return Material.IRON_INGOT;
            case RAW_GOLD: return Material.GOLD_INGOT;
            case RAW_COPPER: return Material.COPPER_INGOT;
            case IRON_ORE: return Material.IRON_INGOT; // Deepslate handled? Drops raw usually.
            case GOLD_ORE: return Material.GOLD_INGOT;
            case SAND: return Material.GLASS;
            case COBBLESTONE: return Material.STONE;
            default: return null;
        }
    }
}
