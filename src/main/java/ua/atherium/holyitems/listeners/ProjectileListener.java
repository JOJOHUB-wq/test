package ua.atherium.holyitems.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomItem;

public class ProjectileListener implements Listener {

    private final HolyWorldItems plugin;
    private final NamespacedKey customItemKey;

    public ProjectileListener(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.customItemKey = new NamespacedKey(plugin, "custom_projectile_id");
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        ItemStack itemStack = player.getInventory().getItemInMainHand(); // Or offhand? Projectiles usually launch from used hand.
        // Simplified: Check both or just MainHand if interacting.
        // Actually, for Snowball/EnderPearl, it consumes item from hand.
        // For Bow/Trident, it's the weapon.

        CustomItem item = plugin.getItemManager().getItem(itemStack);
        if (item != null) {
            event.getEntity().getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, item.getId());
        }

        // Trident Homing
        if (event.getEntity() instanceof Trident) {
            Trident trident = (Trident) event.getEntity();
            if (plugin.getEnchantmentManager().hasEnchant(itemStack, "HOMING")) {
                int level = plugin.getEnchantmentManager().getEnchantLevel(itemStack, "HOMING");
                startHoming(trident, player, level);
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getHitEntity();
            Projectile projectile = event.getEntity();

            String id = projectile.getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
            if (id != null) {
                CustomItem item = plugin.getItemManager().getItem(id);
                if (item != null && item.getId().equals("snowball")) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1));
                }
            }

            // Stun Enchantment (Bow/Crossbow)
            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();
                ItemStack weapon = shooter.getInventory().getItemInMainHand(); // Simplified
                if (plugin.getEnchantmentManager().hasEnchant(weapon, "STUN")) {
                     if (new java.util.Random().nextInt(100) < 5) {
                         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3)); // IV = amplifier 3
                         target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                         shooter.sendMessage(ua.atherium.holyitems.utils.ChatUtil.color("&aПротивник оглушен!"));
                     }
                }
            }
        }
    }

    private void startHoming(Trident trident, Player shooter, int level) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trident.isDead() || trident.isInBlock() || trident.isOnGround()) {
                    this.cancel();
                    return;
                }

                Entity target = null;
                double minDistance = 100.0;

                for (Entity e : trident.getNearbyEntities(10, 10, 10)) {
                    if (e instanceof LivingEntity && e != shooter && e != trident) {
                        double dist = e.getLocation().distance(trident.getLocation());
                        if (dist < minDistance) {
                            minDistance = dist;
                            target = e;
                        }
                    }
                }

                if (target != null) {
                    Vector direction = target.getLocation().toVector().subtract(trident.getLocation().toVector()).normalize();
                    double speed = trident.getVelocity().length();
                    double strength = 0.25 * level;
                    Vector newVel = trident.getVelocity().add(direction.multiply(strength)).normalize().multiply(speed);
                    trident.setVelocity(newVel);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
