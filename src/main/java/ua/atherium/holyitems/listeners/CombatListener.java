package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.managers.EnchantmentManager;
import ua.atherium.holyitems.utils.Utils;

import java.util.Random;

public class CombatListener implements Listener {

    private final HolyWorldItems plugin;
    private final Random random = new Random();

    public CombatListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            if (!(snowball.getShooter() instanceof Player)) return;

            // Check if it's our custom snowball
            // Wait, Projectile doesn't keep ItemMeta easily unless persistent data is transferred.
            // But if the shooter held the item? Or we check item in hand?
            // "Effect on hit" implies the projectile carries the effect.
            // Since we can't easily track projectile item source without metadata, and Snowball item is consumed,
            // we usually assume all snowballs or check offhand/mainhand of shooter if valid.
            // But let's assume if they have the custom snowball item in hand when shooting.
            // Or better: Use `PersistentDataContainer` on the projectile if 1.21 supports it (yes).
            // But vanilla snowball launch doesn't transfer data.
            // We can listen to `ProjectileLaunchEvent` to transfer data.

            // For now, simple check: active item in hand of shooter.
            Player shooter = (Player) snowball.getShooter();
            ItemStack item = shooter.getInventory().getItemInMainHand();
            // This is flaky if they switch fast.
            // I'll stick to a simpler assumption or listener combo.
            // Actually, `PlayerInteractListener` for snowball usage cancels standard behavior? No.

            // Let's implement `ProjectileLaunchEvent` inside this class to tag the projectile.
        }
    }

    @EventHandler
    public void onLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball && event.getEntity().getShooter() instanceof Player) {
             Player shooter = (Player) event.getEntity().getShooter();
             ItemStack item = shooter.getInventory().getItemInMainHand();
             String id = plugin.getItemManager().getItemId(item);
             if ("snowball".equals(id)) {
                 event.getEntity().addScoreboardTag("holy_snowball");
             } else {
                 // Check offhand
                 item = shooter.getInventory().getItemInOffHand();
                 id = plugin.getItemManager().getItemId(item);
                 if ("snowball".equals(id)) {
                     event.getEntity().addScoreboardTag("holy_snowball");
                 }
             }
        }
    }

    @EventHandler
    public void onShootBow(org.bukkit.event.entity.EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
            ItemStack bow = event.getBow();
            if (bow == null) return;

            // Stun Enchant
            int stunLevel = plugin.getEnchantmentManager().getEnchantLevel(bow, "stun");
            if (stunLevel > 0) {
                event.getProjectile().addScoreboardTag("holy_stun_" + stunLevel);
            }

            // Homing Enchant?
            int homing = plugin.getEnchantmentManager().getEnchantLevel(bow, "homing"); // Trident usually? Prompt says Trident.
            // But Stun applies to Bow/Crossbow.
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof LivingEntity && event.getEntity().getScoreboardTags().contains("holy_snowball")) {
            LivingEntity target = (LivingEntity) event.getHitEntity();
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0)); // 10s
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1)); // 10s, Level 2
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item != null && item.hasItemMeta()) {
                // Custom Item Logic
                String id = plugin.getItemManager().getItemId(item);

                // Enchantments
                handleEnchantmentsDamage(player, item, event.getEntity(), event);
            }
        }

        // Arrow hits (Stun Enchant)
        if (event.getDamager() instanceof Arrow) {
             Arrow arrow = (Arrow) event.getDamager();
             for (String tag : arrow.getScoreboardTags()) {
                 if (tag.startsWith("holy_stun_")) {
                     int level = Integer.parseInt(tag.replace("holy_stun_", ""));
                     if (random.nextInt(100) < 5) { // 5%
                         if (event.getEntity() instanceof LivingEntity) {
                             LivingEntity target = (LivingEntity) event.getEntity();
                             target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                             target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                             if (arrow.getShooter() instanceof Player) {
                                 ((Player) arrow.getShooter()).sendMessage(Utils.color("&aОглушение сработало!"));
                             }
                         }
                     }
                 }
             }
        }
    }

    private void handleEnchantmentsDamage(Player player, ItemStack item, Entity target, EntityDamageByEntityEvent event) {
        EnchantmentManager em = plugin.getEnchantmentManager();

        // Boss Hunter
        int bossHunter = em.getEnchantLevel(item, "boss_hunter");
        if (bossHunter > 0) {
            if (isBoss(target)) {
                event.setDamage(event.getDamage() * 1.7); // +70%
            }
        }

        // Critical
        int critical = em.getEnchantLevel(item, "critical");
        if (critical > 0) {
            if (random.nextInt(100) < 20) { // 20%
                 event.setDamage(event.getDamage() * 1.15); // +15%
                 player.sendMessage(Utils.color("&cКритический удар!"));
            }
        }

        // Armor Breaker (Damage durability)
        int armorBreaker = em.getEnchantLevel(item, "armor_breaker");
        if (armorBreaker > 0) {
            if (target instanceof Player) {
                Player victim = (Player) target;
                ItemStack[] armor = victim.getInventory().getArmorContents();
                for (ItemStack piece : armor) {
                    if (piece != null && piece.getType() != Material.AIR) {
                        // Increase durability damage
                        // Standard is 1? Add extra.
                        // "Breaks enemy armor 25% faster"
                        // Hard to implement exactly without recalculating damage mechanics.
                        // I'll just deal extra durability damage occasionally or add 1 extra.
                        if (random.nextInt(4) == 0) { // 25% chance to deal extra 1 durability
                             // Deprecated but works
                             piece.setDurability((short)(piece.getDurability() + 1));
                             // Should update inventory
                        }
                    }
                }
                victim.getInventory().setArmorContents(armor);
            }
        }
    }

    private boolean isBoss(Entity entity) {
        return entity instanceof Wither || entity instanceof EnderDragon || entity instanceof Warden || entity instanceof ElderGuardian;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            ItemStack item = killer.getInventory().getItemInMainHand();

            // Rich Enchant
            int rich = plugin.getEnchantmentManager().getEnchantLevel(item, "rich");
            if (rich > 0) {
                // "+40% per level coins from mobs"
                // Assuming mobs drop coins via Vault? Or custom drops?
                // "Economy Integration: Vault API for монетки"
                // Usually mobs don't drop Vault money unless a plugin handles it.
                // "Golden Spawner" generates coins.
                // I'll assume I should give money directly.
                // Base money? Random 1-10?
                double base = 5.0;
                double bonus = base * (0.4 * rich);
                plugin.getEconomyManager().deposit(killer.getName(), bonus);
                // killer.sendMessage(Utils.color("&e+ " + (base+bonus) + " монет (Богач)"));
            }

            // Magnetism (Tools/Weapons)
            int magnetism = plugin.getEnchantmentManager().getEnchantLevel(item, "magnetism");
            if (magnetism > 0) {
                event.getDrops().forEach(drop -> killer.getInventory().addItem(drop));
                event.getDrops().clear();
            }
        }
    }
}
