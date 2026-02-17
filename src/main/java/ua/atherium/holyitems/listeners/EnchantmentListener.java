package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.managers.EnchantmentManager;
import ua.atherium.holyitems.utils.Utils;

import java.util.Random;

public class EnchantmentListener implements Listener {

    private final HolyWorldItems plugin;
    private final Random random = new Random();

    public EnchantmentListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Unbreakable (Armor)
            // "5% chance to take 20% less damage (stacks across armor pieces)"
            // "Max Stacked: 20% chance (all 4 armor pieces)" - wait, prompt says "Max Stacked: 20% chance".
            // Logic: 5% per piece -> 20% total chance.
            // If procs -> 20% damage reduction.

            int unbreakableCount = 0;
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && plugin.getEnchantmentManager().getEnchantLevel(armor, "unbreakable") > 0) {
                    unbreakableCount++;
                }
            }

            if (unbreakableCount > 0) {
                int chance = unbreakableCount * 5; // 5, 10, 15, 20
                if (random.nextInt(100) < chance) {
                    event.setDamage(event.getDamage() * 0.8); // 20% reduction
                    // player.sendMessage(Utils.color("&aСработал Непробиваемый!")); // Spammy?
                }
            }
        }
    }

    @EventHandler
    public void onMend(PlayerItemMendEvent event) {
        // Mending II (Repair 2x faster)
        // Standard mending repairs 2 durability per XP point.
        // Mending II -> 4 durability?
        // Or modify XP cost?
        // Event has getRepairAmount().
        ItemStack item = event.getItem();
        if (plugin.getEnchantmentManager().getEnchantLevel(item, "mending_2") > 0) {
             event.setRepairAmount(event.getRepairAmount() * 2);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();

        // Lava Walker
        if (boots != null && plugin.getEnchantmentManager().getEnchantLevel(boots, "lava_walker") > 0) {
             Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
             if (block.getType() == Material.LAVA) {
                 // Convert to Obsidian temporarily?
                 // Like Frost Walker.
                 // Need to schedule reversion.
                 // Only if stationary lava (source)?
                 if (block.getBlockData() instanceof Levelled) {
                     // Check level 0 (source) usually?
                     // Frost walker works on water sources.
                     // But prompt says "Walk on lava".
                     // Temporarily change block to Obsidian/Magma.
                     // Must use scheduler to revert.
                     // Using specific block metadata or manager to track temporary blocks.
                     // Simplistic implementation: Change to Obsidian, revert after 5s.

                     // Check if not already changed
                     // Check WorldGuard region?
                     if (!plugin.getRegionManager().canBuild(player, block.getLocation())) return;

                     block.setType(Material.OBSIDIAN);
                     plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                         if (block.getType() == Material.OBSIDIAN) {
                             block.setType(Material.LAVA);
                         }
                     }, 100L); // 5s
                 }
             }
        }
    }
}
