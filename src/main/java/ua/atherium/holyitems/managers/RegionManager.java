package ua.atherium.holyitems.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionManager {

    private final JavaPlugin plugin;
    private Object wgHandler;
    private Object gpHandler;

    public RegionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                wgHandler = new WorldGuardHandler();
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to initialize WorldGuard support: " + t.getMessage());
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            try {
                gpHandler = new GriefPreventionHandler();
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to initialize GriefPrevention support: " + t.getMessage());
            }
        }
    }

    public boolean canBuild(Player player, Location location) {
        if (player.hasPermission("holyitems.bypass-region")) return true;

        if (wgHandler != null && !((WorldGuardHandler) wgHandler).canBuild(player, location)) {
            return false;
        }
        if (gpHandler != null && !((GriefPreventionHandler) gpHandler).canBuild(player, location)) {
            return false;
        }
        return true;
    }

    // Inner classes to prevent class loading issues if dependencies are missing

    private static class WorldGuardHandler {
        public boolean canBuild(Player player, Location location) {
            com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            return query.testState(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location), com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player), com.sk89q.worldguard.protection.flags.Flags.BUILD);
        }
    }

    private static class GriefPreventionHandler {
        public boolean canBuild(Player player, Location location) {
            me.ryanhamshire.GriefPrevention.DataStore dataStore = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
            me.ryanhamshire.GriefPrevention.Claim claim = dataStore.getClaimAt(location, true, null);
            if (claim == null) return true;
            return claim.allowBuild(player, org.bukkit.Material.STONE) == null;
        }
    }
}
