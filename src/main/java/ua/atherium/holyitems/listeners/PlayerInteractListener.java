package ua.atherium.holyitems.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.managers.CooldownManager;
import ua.atherium.holyitems.managers.ItemManager;
import ua.atherium.holyitems.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final HolyWorldItems plugin;
    private final Map<UUID, Long> stunActive = new HashMap<>(); // Player UUID -> End Time (radius check needed)
    private final Map<Location, Long> stunZones = new HashMap<>(); // Center -> End Time

    public PlayerInteractListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        String id = plugin.getItemManager().getItemId(item);
        if (id == null) return;

        // Check Cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, id)) {
            long seconds = plugin.getCooldownManager().getRemainingSeconds(player, id);
            player.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.cooldown_active").replace("{time}", String.valueOf(seconds))));
            event.setCancelled(true);
            return;
        }

        boolean used = false;

        switch (id) {
            case "stan":
                used = handleStun(player);
                break;
            case "jack_o_lantern":
                used = handleJakeLantern(player);
                break;
            case "explosive_rod":
                used = handleExplosiveRod(player);
                break;
            case "echo_shard":
                used = handleEchoShard(player);
                break;
            case "explosive_thing":
                used = handleExplosiveThing(player);
                break;
            case "special_compass":
                used = handleCompass(player);
                break;
            case "experience_bubble":
                used = handleXPBubble(player);
                break;
            case "trap":
                used = handleTrap(player, event);
                break;
            case "explosive_trap":
                used = handleExplosiveTrap(player, event);
                break;
            case "mysterious_spawn_egg":
                used = handleSpawnEgg(player, event);
                break;
            case "universal_key":
                used = handleKey(player, event);
                break;
        }

        if (used) {
            // Set Cooldown
            int cooldown = plugin.getConfigManager().getConfig("items.yml").getInt("items." + id + ".cooldown", 0);
            if (cooldown > 0) {
                plugin.getCooldownManager().setCooldown(player, id, cooldown);
            }

            // Consume if needed
            if (plugin.getConfigManager().getConfig("items.yml").getBoolean("items." + id + ".consume", false)) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    private boolean handleStun(Player player) {
        // Radius 15, Duration 15s
        int radius = 15;
        int duration = 15;

        // Add Stun Zone
        stunZones.put(player.getLocation(), System.currentTimeMillis() + (duration * 1000));

        // Apply Slowness 2 to nearby enemies
        for (Player target : player.getWorld().getPlayers()) {
            if (target.getLocation().distance(player.getLocation()) <= radius && !target.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 1)); // Level 2 is amplifier 1
                target.sendMessage(Utils.color("&cВы были оглушены!"));
            }
        }

        player.sendMessage(Utils.color("&aСтан активирован!"));
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
        return true;
    }

    private boolean handleJakeLantern(Player player) {
        int radius = 20;
        int duration = 15;

        for (Player target : player.getWorld().getPlayers()) {
            if (target.getLocation().distance(player.getLocation()) <= radius && !target.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 0));

                // Pumpkin Helmet if no helmet
                ItemStack helmet = target.getInventory().getHelmet();
                if (helmet == null || helmet.getType() == Material.AIR) {
                    target.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
                    // Maybe schedule removal? For now, simplistic implementation.
                    // To do it properly, I need a task.
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (target.isOnline() && target.getInventory().getHelmet() != null && target.getInventory().getHelmet().getType() == Material.CARVED_PUMPKIN) {
                            target.getInventory().setHelmet(null);
                        }
                    }, duration * 20L);
                }
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_PUMPKIN_CARVE, 1f, 1f);
        return true;
    }

    private boolean handleExplosiveRod(Player player) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setYield(0); // No block damage by default fireball?
        fireball.setIsIncendiary(false);
        fireball.setVelocity(player.getLocation().getDirection().multiply(2));
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        return true;
    }

    private boolean handleEchoShard(Player player) {
        int radius = 10;
        double strength = 2.5;

        for (Player target : player.getWorld().getPlayers()) {
            if (target.getLocation().distance(player.getLocation()) <= radius && !target.equals(player)) {
                Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                target.setVelocity(direction.multiply(strength));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f);
        return true;
    }

    private boolean handleExplosiveThing(Player player) {
        // Directional explosion
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        player.getWorld().createExplosion(loc, 3.0f, false, false); // No fire, no block break?
        // "Explosion Power: 3.0". Assuming createExplosion handles damage.
        return true;
    }

    private boolean handleCompass(Player player) {
        // Use structure locator for 1.21
        try {
            // For 1.21 this might require Structure object or different method
            // Keeping it simple with basic check or fallback
            // Using deprecated for now as StructureType is still often supported or easily mapped
            // But ideally use Registry.STRUCTURE
            org.bukkit.util.StructureSearchResult result = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.generator.structure.Structure.BURIED_TREASURE, 100, false);
            if (result != null) {
                Location loc = result.getLocation();
                player.setCompassTarget(loc);
                player.sendMessage(Utils.color("&aКомпас указывает на ближайшее сокровище: " + loc.getBlockX() + ", " + loc.getBlockZ()));
            } else {
                player.sendMessage(Utils.color("&cСокровище не найдено поблизости."));
            }
        } catch (Exception e) {
             player.sendMessage(Utils.color("&cОшибка поиска структуры (версия сервера?)."));
        }
        return true;
    }

    private boolean handleXPBubble(Player player) {
        // Repair Mending armor
        boolean repaired = false;
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack armor : armorContents) {
            if (armor != null && armor.getType() != Material.AIR) {
                if (armor.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.MENDING) > 0) {
                     if (armor.getDurability() > 0) {
                         armor.setDurability((short) 0);
                         repaired = true;
                     }
                }
            }
        }

        if (repaired) {
            player.getInventory().setArmorContents(armorContents);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.sendMessage(Utils.color("&aВаша броня починена!"));
            return true;
        } else {
            player.sendMessage(Utils.color("&cНет брони для починки!"));
            return false;
        }
    }

    private boolean handleTrap(Player player, PlayerInteractEvent event) {
        if (!plugin.getRegionManager().canBuild(player, player.getLocation())) {
            player.sendMessage(Utils.color("&cЗдесь нельзя использовать ловушку (регион)!"));
            event.setCancelled(true);
            return false;
        }

        event.setCancelled(true); // Prevent placement if clicking block

        // Spawn 3x3x3 Cobweb at location
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation().add(0, 1, 0) : player.getLocation();
        // Or centered on player if RIGHT_CLICK_AIR?
        // Usually trap is thrown or placed? "Material: COBWEB". Usually placed.
        // If "Trap" implies trapping SOMEONE ELSE, usually you click them or throw.
        // But if it's "Material: COBWEB", it behaves like a block.
        // Let's assume placement at clicked block or self if air.

        // Structure
        createTemporaryStructure(loc, 3, 3, Material.COBWEB, 30);
        player.sendMessage(Utils.color("&aЛовушка установлена!"));
        return true;
    }

    private boolean handleExplosiveTrap(Player player, PlayerInteractEvent event) {
        if (!plugin.getRegionManager().canBuild(player, player.getLocation())) {
            player.sendMessage(Utils.color("&cЗдесь нельзя использовать ловушку (регион)!"));
            event.setCancelled(true);
            return false;
        }
        event.setCancelled(true);

        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation().add(0, 1, 0) : player.getLocation();

        if (loc.getBlock().isLiquid()) {
            // Water sphere
             createTemporarySphere(loc, 5, Material.GLASS, 45);
        } else {
            // Land structure 5x5x3
             createTemporaryStructure(loc, 5, 3, Material.OBSIDIAN, 45);
        }

        player.sendMessage(Utils.color("&cВзрывная трапка установлена!"));
        return true;
    }

    private void createTemporaryStructure(Location center, int radius, int height, Material mat, int duration) {
        Map<Location, org.bukkit.block.data.BlockData> original = new HashMap<>();
        int r = radius / 2; // e.g. 3 -> 1 (center + 1 + 1 = 3)

        for (int x = -r; x <= r; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -r; z <= r; z++) {
                    Location l = center.clone().add(x, y, z);
                    if (l.getBlock().getType() == Material.AIR || l.getBlock().getType().isBurnable()) { // Replace only air/weak blocks
                        original.put(l, l.getBlock().getBlockData());
                        l.getBlock().setType(mat);
                    }
                }
            }
        }

        // Restore task
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Map.Entry<Location, org.bukkit.block.data.BlockData> entry : original.entrySet()) {
                entry.getKey().getBlock().setBlockData(entry.getValue());
            }
        }, duration * 20L);
    }

    private void createTemporarySphere(Location center, int radius, Material mat, int duration) {
        Map<Location, org.bukkit.block.data.BlockData> original = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z <= radius*radius) {
                        Location l = center.clone().add(x, y, z);
                         if (l.getBlock().getType() == Material.WATER || l.getBlock().getType() == Material.AIR) {
                            original.put(l, l.getBlock().getBlockData());
                            l.getBlock().setType(mat);
                        }
                    }
                }
            }
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Map.Entry<Location, org.bukkit.block.data.BlockData> entry : original.entrySet()) {
                entry.getKey().getBlock().setBlockData(entry.getValue());
            }
        }, duration * 20L);
    }

    private boolean handleSpawnEgg(Player player, PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.SPAWNER) {
            event.setCancelled(true);
            // Spawn 3 custom mobs
            Location loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

            // 1. Reinforced Zombie
            org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ZOMBIE);
            zombie.setCustomName(Utils.color("&cУсиленный зомби"));
            zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            zombie.setHealth(40);
            zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(8);

            // 2. Fire Skeleton
            org.bukkit.entity.Skeleton skeleton = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.SKELETON);
            skeleton.setCustomName(Utils.color("&6Огненный скелет"));
            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW)); // Flame enchant needed?
            // Add flame enchant
            ItemStack bow = new ItemStack(Material.BOW);
            bow.addEnchantment(org.bukkit.enchantments.Enchantment.FLAME, 1);
            skeleton.getEquipment().setItemInMainHand(bow);
            skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 99999, 0));

            // 3. Ender Creeper
            org.bukkit.entity.Creeper creeper = (org.bukkit.entity.Creeper) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.CREEPER);
            creeper.setCustomName(Utils.color("&dЭндер-крипер"));
            creeper.setExplosionRadius(5);
            // Teleport logic needs listener (EntityDamageEvent -> teleport)

            player.sendMessage(Utils.color("&aМобы призваны!"));
            return true;
        }
        return false;
    }

    private boolean handleKey(Player player, PlayerInteractEvent event) {
        // "Opens special dungeons and summons bosses"
        // Without a dungeon plugin, maybe just summons a random boss at location?
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation().add(0, 2, 0) : player.getLocation();

        org.bukkit.entity.Wither wither = (org.bukkit.entity.Wither) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.WITHER);
        wither.setCustomName(Utils.color("&4&lБОСС ПОДЗЕМЕЛЬЯ"));

        player.sendMessage(Utils.color("&dВы призвали босса!"));
        return true;
    }

    // Getters for Stun Logic
    public boolean isStunned(Location loc) {
        // Check if loc is in any active stun zone
        // Avoid modification during iteration
        long now = System.currentTimeMillis();
        for (Map.Entry<Location, Long> entry : stunZones.entrySet()) {
             if (now < entry.getValue()) {
                 if (entry.getKey().getWorld().equals(loc.getWorld()) && entry.getKey().distance(loc) <= 15) {
                     return true;
                 }
             }
        }
        return false;
    }

    public void cleanupStunZones() {
        stunZones.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());
    }
}
