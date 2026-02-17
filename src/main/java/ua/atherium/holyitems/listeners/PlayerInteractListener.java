package ua.atherium.holyitems.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomItem;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerInteractListener implements Listener {

    private final HolyWorldItems plugin;

    public PlayerInteractListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        CustomItem item = plugin.getItemManager().getItem(itemStack);
        if (item == null) return;

        if (item.isPlaceable()) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // Region Check
            if (!plugin.getRegionManager().canBuild(player, player.getLocation())) {
                if (item.getId().contains("trap") || item.getId().contains("explosive")) {
                    player.sendMessage(ChatUtil.color("&cЗдесь нельзя использовать этот предмет!"));
                    event.setCancelled(true);
                    return;
                }
            }

            // Cooldown Check
            if (item.getCooldown() > 0) {
                if (plugin.getCooldownManager().hasCooldown(player, item.getId())) {
                    long left = plugin.getCooldownManager().getRemainingSeconds(player, item.getId());
                    player.sendMessage(ChatUtil.color("&cКулдаун активен! Осталось: &e" + left + " сек"));
                    event.setCancelled(true);
                    return;
                }
            }

            // Handle Effects
            boolean used = false;
            Map<String, Object> effects = item.getEffects();

            if (effects.containsKey("shoot_fireball")) {
                handleFireball(player, (Map<String, Object>) effects.get("shoot_fireball"));
                used = true;
            }
            if (effects.containsKey("knockback")) {
                handleKnockback(player, (Map<String, Object>) effects.get("knockback"));
                used = true;
            }
            if (effects.containsKey("glowing")) {
                handleGlowing(player, (Map<String, Object>) effects.get("glowing"));
                if (effects.containsKey("pumpkin_helmet")) {
                    handlePumpkinHelmet(player, (Map<String, Object>) effects.get("pumpkin_helmet"));
                }
                used = true;
            }
            if (effects.containsKey("block_teleport")) {
                handleBlockTeleport(player, (Map<String, Object>) effects.get("block_teleport"));
                if (effects.containsKey("slowness")) {
                    handleSlowness(player, (Map<String, Object>) effects.get("slowness"));
                }
                used = true;
            }
            if (effects.containsKey("directional_explosion")) {
                handleExplosion(player, (Map<String, Object>) effects.get("directional_explosion"));
                used = true;
            }
            if (effects.containsKey("create_cube")) {
                handleCreateCube(player, event.getClickedBlock(), (Map<String, Object>) effects.get("create_cube"));
                used = true;
            }
            if (effects.containsKey("land_structure") || effects.containsKey("water_sphere")) {
                handleExplosiveTrap(player, event.getClickedBlock(), effects);
                used = true;
            }
             if (effects.containsKey("show_treasure_coords")) {
                player.sendMessage(ChatUtil.color("&aКоординаты сокровищницы: " + (player.getLocation().getBlockX() + 500) + ", " + (player.getLocation().getBlockZ() + 500)));
                used = true;
            }

            if (used) {
                if (item.getCooldown() > 0) {
                    plugin.getCooldownManager().setCooldown(player, item.getId(), item.getCooldown());
                }
                if (item.isConsume()) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                }
                event.setCancelled(true);
            }
        }
    }

    private void handleFireball(Player player, Map<String, Object> data) {
        double speed = getDouble(data, "speed", 2.0);
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(player.getLocation().getDirection().multiply(speed));
        fireball.setYield(0);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
    }

    private void handleKnockback(Player player, Map<String, Object> data) {
        int radius = getInt(data, "radius", 10);
        double strength = getDouble(data, "strength", 2.5);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != player) {
                Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(strength));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
    }

    private void handleGlowing(Player player, Map<String, Object> data) {
        int radius = getInt(data, "radius", 20);
        int duration = getInt(data, "duration", 15);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 0));
            }
        }
    }

    private void handlePumpkinHelmet(Player player, Map<String, Object> data) {
        int duration = getInt(data, "duration", 15);
        boolean requireNoHelmet = (boolean) data.getOrDefault("require_no_helmet", true);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player && entity != player) {
                Player target = (Player) entity;
                ItemStack helm = target.getInventory().getHelmet();
                if (!requireNoHelmet || (helm == null || helm.getType() == Material.AIR)) {
                    target.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                         if (target.getInventory().getHelmet() != null && target.getInventory().getHelmet().getType() == Material.CARVED_PUMPKIN) {
                             target.getInventory().setHelmet(null);
                         }
                    }, duration * 20L);
                }
            }
        }
    }

    private void handleBlockTeleport(Player player, Map<String, Object> data) {
        int radius = getInt(data, "radius", 15);
        int duration = getInt(data, "duration", 15);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != player) {
                plugin.getCooldownManager().setCooldown((Player) entity, "teleport_blocked", duration);
                entity.sendMessage(ChatUtil.color("&cТелепортация заблокирована на " + duration + " сек!"));
            }
        }
    }

    private void handleSlowness(Player player, Map<String, Object> data) {
        int radius = getInt(data, "radius", 15);
        int duration = getInt(data, "duration", 15);
        int level = getInt(data, "level", 2);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && entity != player) {
                ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, level - 1));
            }
        }
    }

    private void handleExplosion(Player player, Map<String, Object> data) {
        double power = getDouble(data, "power", 3.0);
        int range = getInt(data, "range", 15);
        Block target = player.getTargetBlockExact(range);
        if (target != null) {
            player.getWorld().createExplosion(target.getLocation(), (float) power, false, false);
        }
    }

    private void handleCreateCube(Player player, Block clicked, Map<String, Object> data) {
        Location center = clicked != null ? clicked.getLocation() : player.getLocation();
        int size = getInt(data, "size", 3);
        int duration = getInt(data, "duration", 30);
        Material mat = plugin.getCooldownManager().getTrapMaterial(player);

        List<Block> changed = new ArrayList<>();
        int r = size / 2;

        for (int x = -r; x <= r; x++) {
             for (int y = 0; y < size; y++) {
                 for (int z = -r; z <= r; z++) {
                     Block b = center.clone().add(x, y, z).getBlock();
                     if (b.getType() == Material.AIR) {
                         b.setType(mat);
                         changed.add(b);
                     }
                 }
             }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Block b : changed) {
                if (b.getType() == mat) b.setType(Material.AIR);
            }
        }, duration * 20L);
    }

    private void handleExplosiveTrap(Player player, Block clicked, Map<String, Object> effects) {
        Location center = clicked != null ? clicked.getLocation() : player.getLocation();
        boolean inWater = center.getBlock().getType() == Material.WATER;

        if (inWater && effects.containsKey("water_sphere")) {
            Map<String, Object> data = (Map<String, Object>) effects.get("water_sphere");
            int radius = getInt(data, "radius", 5);
            int duration = getInt(data, "duration", 45);
            Material mat = Material.GLASS;

            List<Block> changed = new ArrayList<>();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x*x + y*y + z*z <= radius*radius) {
                             Block b = center.clone().add(x, y, z).getBlock();
                             if (b.getType() == Material.WATER || b.getType() == Material.AIR) {
                                 b.setType(mat);
                                 changed.add(b);
                             }
                        }
                    }
                }
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Block b : changed) {
                    if (b.getType() == mat) b.setType(Material.WATER);
                }
            }, duration * 20L);

        } else if (!inWater && effects.containsKey("land_structure")) {
            Map<String, Object> data = (Map<String, Object>) effects.get("land_structure");
            int duration = getInt(data, "duration", 45);
            Material mat = Material.OBSIDIAN;
            // 5x5x3
             List<Block> changed = new ArrayList<>();
             for (int x = -2; x <= 2; x++) {
                 for (int y = 0; y < 3; y++) {
                     for (int z = -2; z <= 2; z++) {
                         Block b = center.clone().add(x, y, z).getBlock();
                         if (b.getType() == Material.AIR) {
                             b.setType(mat);
                             changed.add(b);
                         }
                     }
                 }
             }
             plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Block b : changed) {
                    if (b.getType() == mat) b.setType(Material.AIR);
                }
            }, duration * 20L);
        }
    }

    private int getInt(Map<String, Object> data, String key, int def) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return def;
    }

    private double getDouble(Map<String, Object> data, String key, double def) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return def;
    }
}
