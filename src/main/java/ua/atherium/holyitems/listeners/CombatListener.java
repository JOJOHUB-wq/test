package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.CustomEnchantment;
import ua.atherium.holyitems.objects.Sphere;
import ua.atherium.holyitems.objects.SphereEffect;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Map;
import java.util.Random;

public class CombatListener implements Listener {

    private final HolyWorldItems plugin;
    private final Random random = new Random();

    public CombatListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            handleAttack((Player) event.getDamager(), event.getEntity(), event);
        } else if (event.getDamager() instanceof Trident) {
            Trident trident = (Trident) event.getDamager();
            if (trident.getShooter() instanceof Player) {
                handleAttack((Player) trident.getShooter(), event.getEntity(), event);
            }
        }

        if (event.getEntity() instanceof Player) {
            handleDefense((Player) event.getEntity(), event);
        }
    }

    private void handleAttack(Player attacker, Entity victim, EntityDamageByEntityEvent event) {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType() == Material.AIR) return;

        // Custom Enchants
        if (plugin.getEnchantmentManager().hasEnchant(weapon, "BOSS_HUNTER")) {
            if (victim instanceof org.bukkit.entity.Boss || victim.getType().name().contains("WITHER") || victim.getType().name().contains("DRAGON") || victim.getType().name().contains("WARDEN")) {
                int level = plugin.getEnchantmentManager().getEnchantLevel(weapon, "BOSS_HUNTER");
                // Base effect says +70% at level 7 (max). Assume 10% per level?
                // Prompt: Level VII, +70% damage. So 10% per level.
                double boost = 1.0 + (0.10 * level);
                event.setDamage(event.getDamage() * boost);
            }
        }

        if (plugin.getEnchantmentManager().hasEnchant(weapon, "CRITICAL")) {
            int level = plugin.getEnchantmentManager().getEnchantLevel(weapon, "CRITICAL");
            // 20% chance to deal +15% damage (Level 2? Prompt says Level II, 20% chance, +15% damage)
            // Maybe chance increases or damage? Prompt: "20% chance to deal +15% damage".
            // I'll use 10% chance per level.
            if (random.nextInt(100) < (10 * level)) {
                event.setDamage(event.getDamage() * 1.15);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            }
        }

        if (plugin.getEnchantmentManager().hasEnchant(weapon, "ARMOR_BREAKER")) {
            int level = plugin.getEnchantmentManager().getEnchantLevel(weapon, "ARMOR_BREAKER");
            // Breaks armor 25% faster.
            // Implementation: Damage armor durability extra.
            if (victim instanceof Player) {
                Player v = (Player) victim;
                ItemStack[] armor = v.getInventory().getArmorContents();
                for (ItemStack piece : armor) {
                    if (piece != null && piece.getType() != Material.AIR) {
                        damageItem(piece, (int) (event.getDamage() * 0.25 * level)); // Approximate
                    }
                }
            }
        }

        // Stun (Bow/Crossbow) handled in ProjectileHit? Or here if arrow?
        // EntityDamageByEntityEvent damager is Arrow if shot.
    }

    private void handleDefense(Player defender, EntityDamageEvent event) {
        ItemStack[] armor = defender.getInventory().getArmorContents();
        int unbreakablePieces = 0;

        for (ItemStack piece : armor) {
            if (piece != null && plugin.getEnchantmentManager().hasEnchant(piece, "UNBREAKABLE")) {
                unbreakablePieces++;
            }
        }

        if (unbreakablePieces > 0) {
            // 5% chance per piece to take 20% less damage.
            // "Stacks across armor pieces... Max Stacked: 20% chance (all 4 armor pieces)"?
            // Prompt: "5% chance to take 20% less damage (stacks across armor pieces). Max Stacked: 20% chance".
            // So chance = 5 * pieces. Reduction = 20%.
            int chance = 5 * unbreakablePieces;
            if (random.nextInt(100) < chance) {
                event.setDamage(event.getDamage() * 0.8);
                defender.getWorld().playSound(defender.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
            }
        }
    }

    private void damageItem(ItemStack item, int amount) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta().isUnbreakable()) return;
        if (item.getItemMeta() instanceof Damageable) {
            Damageable meta = (Damageable) item.getItemMeta();
            meta.setDamage(meta.getDamage() + amount);
            item.setItemMeta(meta);
            if (meta.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0);
            }
        }
    }
}
