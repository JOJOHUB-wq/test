package com.customenchants.listeners;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BlockBreakHandler implements Listener {

    private final AtheriumEnchants plugin;
    private final Map<Material, Material> smeltMap = new HashMap<>();

    public BlockBreakHandler(AtheriumEnchants plugin) {
        this.plugin = plugin;
        loadSmeltMap();
    }

    private void loadSmeltMap() {
        ConfigurationSection section = plugin.getEnchantmentConfig().getConfig().getConfigurationSection("enchantments.melting");
        if (section == null) return;
        for (String entry : section.getStringList("smelt_map")) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                smeltMap.put(Material.matchMaterial(parts[0].trim()), Material.matchMaterial(parts[1].trim()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (player.getGameMode() == GameMode.CREATIVE || tool.getType() == Material.AIR) {
            return;
        }

        boolean shiftPressed = player.isSneaking() && plugin.getEnchantmentConfig().getConfig().getBoolean("disable_enchants_on_sneak", true);
        Set<Block> affectedBlocks = new HashSet<>();
        affectedBlocks.add(event.getBlock());

        // --- Lumberjack Logic ---
        CustomEnchant lumberjack = plugin.getEnchantmentManager().getEnchantByKey("woodcutter");
        int lumberjackLevel = lumberjack.getLevelFromItem(tool);
        if (lumberjackLevel > 0 && lumberjack.isEnabled() && !shiftPressed && isLog(event.getBlock().getType())) {
            handleLumberjack(event, player, tool, lumberjackLevel);
            return; // Lumberjack has its own logic flow with animation
        }

        // --- Web Logic ---
        CustomEnchant web = plugin.getEnchantmentManager().getEnchantByKey("web");
        int webLevel = web.getLevelFromItem(tool);
        if (webLevel > 0 && web.isEnabled() && !shiftPressed && isOre(event.getBlock().getType())) {
            affectedBlocks.addAll(findVein(event.getBlock(), web.getConfigValue(webLevel, "max_blocks", 30)));
        }

        // --- Bulldozer Logic ---
        CustomEnchant bulldozer = plugin.getEnchantmentManager().getEnchantByKey("bulldozer");
        int bulldozerLevel = bulldozer.getLevelFromItem(tool);
        if (bulldozerLevel > 0 && bulldozer.isEnabled() && !shiftPressed) {
            affectedBlocks.addAll(getBulldozerBlocks(event.getBlock(), player, bulldozerLevel));
        }

        processBlockBreak(affectedBlocks, player, tool, event.getBlock().getLocation());
        event.setDropItems(false);
        event.setExpToDrop(0);
    }

    private void handleLumberjack(BlockBreakEvent event, Player player, ItemStack tool, int level) {
        CustomEnchant lumberjack = plugin.getEnchantmentManager().getEnchantByKey("woodcutter");
        int maxBlocks = lumberjack.getConfigValue(level, "max_blocks", 64);
        long delay = lumberjack.getConfigValue(level, "animation_delay", 1);

        Set<Block> tree = findTree(event.getBlock(), maxBlocks);
        if (tree.size() <= 1) {
             processBlockBreak(tree, player, tool, event.getBlock().getLocation());
             event.setDropItems(false);
             event.setExpToDrop(0);
             return;
        }

        event.setCancelled(true);

        new BukkitRunnable() {
            private final Queue<Block> blocksToBreak = new LinkedList<>(tree);

            @Override
            public void run() {
                if (blocksToBreak.isEmpty() || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                Block block = blocksToBreak.poll();
                if (block != null && !block.getType().isAir()) {
                    processBlockBreak(Set.of(block), player, tool, block.getLocation());
                }
            }
        }.runTaskTimer(plugin, 0L, delay);
    }

    private void processBlockBreak(Set<Block> blocks, Player player, ItemStack tool, Location dropLocation) {
        List<ItemStack> finalDrops = new ArrayList<>();
        int totalExp = 0;

        CustomEnchant autoSmelt = plugin.getEnchantmentManager().getEnchantByKey("melting");
        int autoSmeltLevel = autoSmelt.getLevelFromItem(tool);
        boolean doSmelt = autoSmeltLevel > 0 && autoSmelt.isEnabled() && !player.isSneaking();
        boolean doParticles = doSmelt && autoSmelt.getConfigValue(autoSmeltLevel, "fire_particles", false);

        for (Block block : blocks) {
            if (!plugin.getWorldGuardUtil().canBreakBlock(player, block)) {
                continue;
            }

            totalExp += getExpForBlock(block);
            Collection<ItemStack> drops = block.getDrops(tool);

            if (doSmelt && smeltMap.containsKey(block.getType())) {
                finalDrops.add(new ItemStack(smeltMap.get(block.getType()), 1));
                if (doParticles) {
                    player.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.01);
                }
            } else {
                finalDrops.addAll(drops);
            }

            if(blocks.size() > 1) block.setType(Material.AIR);
        }

        damageTool(player, tool, blocks.size());

        CustomEnchant magnet = plugin.getEnchantmentManager().getEnchantByKey("magnet");
        int magnetLevel = magnet.getLevelFromItem(tool);
        if (magnetLevel > 0 && magnet.isEnabled() && !player.isSneaking()) {
            for (ItemStack drop : finalDrops) {
                player.getInventory().addItem(drop);
            }
        } else {
            for (ItemStack drop : finalDrops) {
                player.getWorld().dropItemNaturally(dropLocation, drop);
            }
        }

        if (totalExp > 0) {
            player.giveExp(totalExp);
        }
    }

    private List<Block> getBulldozerBlocks(Block centerBlock, Player player, int level) {
        List<Block> blocks = new ArrayList<>();
        CustomEnchant bulldozer = plugin.getEnchantmentManager().getEnchantByKey("bulldozer");
        int radius = bulldozer.getConfigValue(level, "radius", 1);
        int depth = bulldozer.getConfigValue(level, "depth", 1);
        List<String> breakableConfig = bulldozer.getConfigStringList(0, "breakable_blocks"); // Not level-specific

        BlockFace face = getPlayerBlockFace(player);
        if (face == null) return blocks;

        int startY = (depth > 1) ? -radius : 0;
        int endY = (depth > 1) ? radius : 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block relative;
                    if (depth > 1) { // 3x3x3
                        relative = centerBlock.getRelative(x, y, z);
                    } else { // 3x3 plane
                        if (face == BlockFace.UP || face == BlockFace.DOWN) {
                            relative = centerBlock.getRelative(x, y, z);
                        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                            relative = centerBlock.getRelative(x, y, 0);
                        } else { // EAST or WEST
                            relative = centerBlock.getRelative(0, y, z);
                        }
                    }
                    if (breakableConfig.contains(relative.getType().name())) {
                        blocks.add(relative);
                    }
                }
            }
        }
        return blocks;
    }

    private Set<Block> findTree(Block startBlock, int maxBlocks) {
        Set<Block> tree = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);
        Material logType = startBlock.getType();

        while (!toCheck.isEmpty() && tree.size() < maxBlocks) {
            Block current = toCheck.poll();
            if (tree.contains(current) || !isLog(current.getType())) {
                continue;
            }
            tree.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = current.getRelative(x, y, z);
                        if (relative.getType() == logType && !tree.contains(relative)) {
                            toCheck.add(relative);
                        }
                    }
                }
            }
        }
        return tree;
    }

    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_WOOD");
    }

    private boolean isOre(Material material) {
        return material.name().contains("_ORE");
    }

    private Set<Block> findVein(Block startBlock, int maxBlocks) {
        Set<Block> vein = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);
        Material oreType = startBlock.getType();

        while (!toCheck.isEmpty() && vein.size() < maxBlocks) {
            Block current = toCheck.poll();
            if (vein.contains(current) || current.getType() != oreType) {
                continue;
            }
            vein.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = current.getRelative(x, y, z);
                        if (relative.getType() == oreType && !vein.contains(relative)) {
                            toCheck.add(relative);
                        }
                    }
                }
            }
        }
        return vein;
    }

    private BlockFace getPlayerBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 10);
        if (lastTwoTargetBlocks.size() < 2) return null;
        return lastTwoTargetBlocks.get(1).getFace(lastTwoTargetBlocks.get(0));
    }

    private void damageTool(Player player, ItemStack tool, int blocksBroken) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
            int damageToApply = 0;

            for (int i = 0; i < blocksBroken; i++) {
                // Chance to avoid durability loss = 1 / (level + 1)
                if (Math.random() > (1.0 / (unbreakingLevel + 1))) {
                    damageToApply++;
                }
            }

            if (damageToApply > 0) {
                int currentDamage = damageable.getDamage();
                if (currentDamage + damageToApply >= tool.getType().getMaxDurability()) {
                    tool.setAmount(0); // Break the tool
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(currentDamage + damageToApply);
                    tool.setItemMeta(meta);
                }
            }
        }
    }

    private int getExpForBlock(Block block) {
        Material type = block.getType();
        if (type == Material.COAL_ORE || type == Material.DEEPSLATE_COAL_ORE) return 1;
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) return 5;
        if (type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) return 5;
        if (type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE) return 3;
        if (type == Material.REDSTONE_ORE || type == Material.DEEPSLATE_REDSTONE_ORE) return 2;
        if (type == Material.SPAWNER) return 20;
        return 0;
    }
}