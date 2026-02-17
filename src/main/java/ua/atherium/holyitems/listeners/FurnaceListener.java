package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

public class FurnaceListener implements Listener {

    private final HolyWorldItems plugin;

    public FurnaceListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBurn(FurnaceBurnEvent event) {
        if (plugin.getMachineManager().isMachine(event.getBlock().getLocation(), "fast_furnace")) {
            // "Fuel Efficiency: 150%"
            // Increase burn time by 50%
            event.setBurnTime((int) (event.getBurnTime() * 1.5));

            // Speed up cook time?
            // FurnaceBurnEvent sets how long FUEL lasts.
            // Cook speed is property of tile entity.
            if (event.getBlock().getState() instanceof Furnace) {
                Furnace furnace = (Furnace) event.getBlock().getState();
                furnace.setCookTimeTotal((int) (furnace.getCookTimeTotal() / 3.0)); // 3x faster? default 200 -> 66
                furnace.update();
            }
        }
    }

    @EventHandler
    public void onStartSmelt(FurnaceStartSmeltEvent event) {
        if (plugin.getMachineManager().isMachine(event.getBlock().getLocation(), "fast_furnace")) {
             // Set cook time total?
             // event.setTotalCookTime?
             event.setTotalCookTime((int) (event.getTotalCookTime() / 3.0));
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        // Sphere smelting -> Shards
        ItemStack source = event.getSource();
        String sphereId = plugin.getSphereManager().getSphereKey() != null && source.hasItemMeta()
                ? source.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getSphereKey(), org.bukkit.persistence.PersistentDataType.STRING)
                : null;

        if (sphereId != null) {
            // It's a sphere!
            String rarity = source.getItemMeta().getPersistentDataContainer().get(plugin.getSphereManager().getRarityKey(), org.bukkit.persistence.PersistentDataType.STRING);
            int amount = 0;
            if ("COMMON".equals(rarity)) amount = 5;
            else if ("EPIC".equals(rarity)) amount = 10;
            else if ("LEGENDARY".equals(rarity)) amount = 20;
            else if ("UNIQUE".equals(rarity)) amount = 50;
            else amount = 5;

            ItemStack shards = new ItemStack(Material.PRISMARINE_SHARD, amount);
            org.bukkit.inventory.meta.ItemMeta meta = shards.getItemMeta();
            meta.setDisplayName(Utils.color("&bОсколок сферы"));
            shards.setItemMeta(meta);

            event.setResult(shards);
        } else {
            // Prevent smelting normal items if they match the dummy recipe input
            if (source.getType() == Material.MAGMA_CREAM || source.getType() == Material.TOTEM_OF_UNDYING) {
                event.setCancelled(true);
            }
        }
    }
}
