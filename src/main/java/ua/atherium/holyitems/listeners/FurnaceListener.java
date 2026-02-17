package ua.atherium.holyitems.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.Sphere;

public class FurnaceListener implements Listener {

    private final HolyWorldItems plugin;

    public FurnaceListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        Sphere sphere = plugin.getSphereManager().getSphere(source);

        if (sphere != null) {
            int shards = sphere.getShardValue();
            if (shards > 0) {
                event.setResult(plugin.getSphereManager().getShardItem(shards));
            }
        }
    }
}
