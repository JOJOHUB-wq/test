package com.customenchants.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardUtil {

    private boolean enabled = false;

    public WorldGuardUtil() {
        Plugin worldGuardPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            enabled = true;
        }
    }

    public boolean canBreakBlock(Player player, Block block) {
        if (!enabled || player.hasPermission("atheriumenchants.admin.bypass-protection")) {
            return true;
        }

        Location loc = block.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.testState(BukkitAdapter.adapt(loc), WorldGuard.getInstance().getPlatform().getSessionManager().get(player).getActor(), Flags.BLOCK_BREAK);
    }
}