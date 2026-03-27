package ua.atherium.holyitems.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;

public class RegionManager {

    private final HolyWorldItems plugin;
    private final boolean worldGuardEnabled;
    private final boolean griefPreventionEnabled;

    public RegionManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        this.worldGuardEnabled = plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard");
        this.griefPreventionEnabled = plugin.getServer().getPluginManager().isPluginEnabled("GriefPrevention");
    }

    public boolean canBuild(Player player, Location location) {
        if (worldGuardEnabled) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            if (!query.testState(BukkitAdapter.adapt(location), WorldGuard.getInstance().getPlatform().getSessionManager().get(BukkitAdapter.adapt(player)), Flags.BUILD)) {
                return false;
            }
        }

        if (griefPreventionEnabled) {
            String result = GriefPrevention.instance.allowBuild(player, location);
            if (result != null) {
                return false;
            }
        }

        return true;
    }

    public boolean canUse(Player player, Location location) {
        // Similar to build but maybe different flags for usage
        return canBuild(player, location);
    }
}
