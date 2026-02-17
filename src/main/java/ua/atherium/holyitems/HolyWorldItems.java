package ua.atherium.holyitems;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.holyitems.managers.*;
import ua.atherium.holyitems.listeners.ChatListener;

import java.util.logging.Level;

public class HolyWorldItems extends JavaPlugin {

    private static HolyWorldItems instance;

    private ConfigManager configManager;
    private ItemManager itemManager;
    private SphereManager sphereManager;
    private EnchantmentManager enchantmentManager;
    private PotionManager potionManager;
    private SkullManager skullManager;
    private CooldownManager cooldownManager;
    private EconomyManager economyManager;
    private RegionManager regionManager;
    private GUIManager guiManager;
    private ChatListener chatListener;

    private final java.util.Set<org.bukkit.Location> goldenSpawners = new java.util.HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();

        // Log start
        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║     HolyWorldItems v" + getDescription().getVersion() + "              ║");
        getLogger().info("║     Кастомные предметы HolyWorld       ║");
        getLogger().info("╚════════════════════════════════════════╝");

        // Initialize Managers
        try {
            this.configManager = new ConfigManager(this);
            this.skullManager = new SkullManager(this);
            this.economyManager = new EconomyManager(this);
            this.regionManager = new RegionManager(this);
            this.cooldownManager = new CooldownManager(this);

            // These will be initialized properly later in the plan
            this.enchantmentManager = new EnchantmentManager(this);
            this.potionManager = new PotionManager(this);
            this.sphereManager = new SphereManager(this);
            this.itemManager = new ItemManager(this);
            this.guiManager = new GUIManager(this);

            this.chatListener = new ChatListener(this);
            getServer().getPluginManager().registerEvents(chatListener, this);

            getLogger().info("✓ Система кулдаунов инициализирована");
            getLogger().info("✓ GUI менеджер загружен");

            // Register Commands
            getCommand("holyitems").setExecutor(new ua.atherium.holyitems.commands.HolyItemsCommand(this));
            getCommand("custom").setExecutor(new ua.atherium.holyitems.commands.CustomCommand(this));
            getCommand("setfilter").setExecutor(new ua.atherium.holyitems.commands.SetFilterCommand(this));
            getCommand("spheres").setExecutor(new ua.atherium.holyitems.commands.SpheresCommand(this));

            // Register Listeners
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.CombatListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.BlockBreakListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.BlockPlaceListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.FurnaceListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.ProjectileListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.AnvilListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.EnchantmentListener(this), this);
            getServer().getPluginManager().registerEvents(new ua.atherium.holyitems.listeners.PlayerConnectionListener(this), this);

            loadSpawners();
            startTasks();

            getLogger().info("✓ HolyWorldItems полностью загружен!");
            getLogger().info("═══════════════════════════════════════");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error initializing HolyWorldItems!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) {
            cooldownManager.saveAll();
        }
        saveSpawners();
        getLogger().info("HolyWorldItems disabled!");
    }

    public static HolyWorldItems getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() { return configManager; }
    public ItemManager getItemManager() { return itemManager; }
    public SphereManager getSphereManager() { return sphereManager; }
    public EnchantmentManager getEnchantmentManager() { return enchantmentManager; }
    public PotionManager getPotionManager() { return potionManager; }
    public SkullManager getSkullManager() { return skullManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public RegionManager getRegionManager() { return regionManager; }
    public GUIManager getGUIManager() { return guiManager; }
    public ChatListener getChatListener() { return chatListener; }

    public java.util.Set<org.bukkit.Location> getGoldenSpawners() { return goldenSpawners; }

    public org.bukkit.NamespacedKey getKey(String key) {
        return new org.bukkit.NamespacedKey(this, key);
    }

    public String getMessage(String path) {
        return ua.atherium.holyitems.utils.ChatUtil.color(
            getConfig().getString("messages." + path, "&cMessage not found: " + path)
        );
    }

    private void loadSpawners() {
        java.io.File file = new java.io.File(getDataFolder(), "spawners.yml");
        if (!file.exists()) return;
        org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        java.util.List<String> list = config.getStringList("locations");
        for (String s : list) {
            try {
                String[] parts = s.split(",");
                goldenSpawners.add(new org.bukkit.Location(getServer().getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
            } catch (Exception ignored) {}
        }
    }

    private void saveSpawners() {
        java.io.File file = new java.io.File(getDataFolder(), "spawners.yml");
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        java.util.List<String> list = new java.util.ArrayList<>();
        for (org.bukkit.Location loc : goldenSpawners) {
            if (loc.getWorld() != null) {
                list.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ());
            }
        }
        config.set("locations", list);
        try { config.save(file); } catch (Exception ignored) {}
    }

    private void startTasks() {
        // Auto-Save Task
        int autoSaveSeconds = getConfig().getInt("settings.auto-save", 300);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (cooldownManager != null) {
                    cooldownManager.saveAll();
                }
            }
        }.runTaskTimerAsynchronously(this, autoSaveSeconds * 20L, autoSaveSeconds * 20L);

        // Golden Spawner Task (Every 5 mins = 6000 ticks)
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.Location loc : goldenSpawners) {
                    if (loc.getChunk().isLoaded()) {
                        // Drop coins (Emerald Dust)
                        ua.atherium.holyitems.objects.CustomItem dust = getItemManager().getItem("emerald_dust");
                        if (dust != null) {
                            org.bukkit.inventory.ItemStack drop = dust.getItemStack();
                            drop.setAmount(10 + new java.util.Random().nextInt(41)); // 10-50
                            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 1, 0.5), drop);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 6000L, 6000L);

        // Haste/Potion Effect Task (Every 20 ticks)
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player p : getServer().getOnlinePlayers()) {
                    org.bukkit.inventory.ItemStack offhand = p.getInventory().getItemInOffHand();
                    ua.atherium.holyitems.objects.Sphere sphere = getSphereManager().getSphere(offhand);
                    if (sphere != null) {
                        for (org.bukkit.potion.PotionEffect effect : sphere.getPotionEffects()) {
                            p.addPotionEffect(effect);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }
}
