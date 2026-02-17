package ua.atherium.holyitems.managers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkullManager {

    private final Map<String, ItemStack> skullCache = new ConcurrentHashMap<>();

    public SkullManager(JavaPlugin plugin) {
    }

    public ItemStack createSkull(String base64) {
        if (skullCache.containsKey(base64)) {
            return skullCache.get(base64).clone();
        }

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
            skull.setItemMeta(meta);
        }

        skullCache.put(base64, skull);
        return skull;
    }

    public ItemStack createSkullFromURL(String urlString) {
        if (urlString == null || urlString.isEmpty()) return new ItemStack(Material.PLAYER_HEAD);

        String base64 = Base64.getEncoder().encodeToString(
            String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", urlString).getBytes()
        );
        return createSkull(base64);
    }

    public CompletableFuture<ItemStack> createSkullAsync(String base64) {
        return CompletableFuture.supplyAsync(() -> createSkull(base64));
    }
}
