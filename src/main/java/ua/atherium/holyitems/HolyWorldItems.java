package ua.atherium.holyitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.holyitems.managers.*;
import ua.atherium.holyitems.commands.HolyItemsCommand;
import ua.atherium.holyitems.commands.SphereCommand;
import ua.atherium.holyitems.commands.CustomCommand;
import ua.atherium.holyitems.commands.SetFilterCommand;
import ua.atherium.holyitems.listeners.*;

import java.util.logging.Level;

public class HolyWorldItems extends JavaPlugin {

    private static HolyWorldItems instance;

    // Managers
    private ConfigManager configManager;
    private ItemManager itemManager;
    private SphereManager sphereManager;
    private EnchantmentManager enchantmentManager;
    private PotionManager potionManager;
    private CooldownManager cooldownManager;
    private RegionManager regionManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;
    private MachineManager machineManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Banner
        getLogger().info("\n" +
                "╔════════════════════════════════════════╗\n" +
                "║     HolyWorldItems v1.0.0              ║\n" +
                "║     Кастомные предметы HolyWorld       ║\n" +
                "╚════════════════════════════════════════╝");

        // 1. Load Configs
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        // 2. Initialize Managers
        this.economyManager = new EconomyManager(this); // Vault
        this.regionManager = new RegionManager(this); // WorldGuard/GP
        this.cooldownManager = new CooldownManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.enchantmentManager = new EnchantmentManager(this);
        this.potionManager = new PotionManager(this);
        this.sphereManager = new SphereManager(this);
        this.sphereManager.registerRecipes(); // Register dummy recipes
        this.itemManager = new ItemManager(this);
        this.machineManager = new MachineManager(this);
        this.guiManager = new GUIManager(this);

        // 3. Register Commands
        HolyItemsCommand hiCmd = new HolyItemsCommand(this);
        getCommand("holyitems").setExecutor(hiCmd);
        getCommand("holyitems").setTabCompleter(hiCmd);

        getCommand("spheres").setExecutor(new SphereCommand(this));
        getCommand("custom").setExecutor(new CustomCommand(this));
        getCommand("setfilter").setExecutor(new SetFilterCommand(this));

        // 4. Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        // PlayerInventoryListener removed (logic in SphereManager task)
        getServer().getPluginManager().registerEvents(new EnchantmentListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new FurnaceListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        // EnchantmentListener removed duplicate registration

        // Status
        getLogger().info("✓ Загружено " + itemManager.getItems().size() + " кастомных предметов");
        getLogger().info("✓ Загружено " + sphereManager.getSpheres().size() + " сфер и талисманов");
        getLogger().info("✓ Загружено " + enchantmentManager.getEnchantments().size() + " кастомных зачарований");
        getLogger().info("✓ Загружено " + potionManager.getPotions().size() + " кастомных зелий");
        getLogger().info("✓ Система кулдаунов инициализирована");
        getLogger().info("✓ GUI менеджер загружен");
        getLogger().info("✓ Команды зарегистрированы");
        getLogger().info("✓ HolyWorldItems полностью загружен!");
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) {
            cooldownManager.saveCooldowns();
        }
        getLogger().info("HolyWorldItems disabled!");
    }

    public static HolyWorldItems getInstance() {
        return instance;
    }

    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public ItemManager getItemManager() { return itemManager; }
    public SphereManager getSphereManager() { return sphereManager; }
    public EnchantmentManager getEnchantmentManager() { return enchantmentManager; }
    public PotionManager getPotionManager() { return potionManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public RegionManager getRegionManager() { return regionManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public MachineManager getMachineManager() { return machineManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
}
