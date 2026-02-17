package ua.atherium.holyitems.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.objects.Sphere;

public class AnvilListener implements Listener {
    private final HolyWorldItems plugin;
    public AnvilListener(HolyWorldItems plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack item1 = event.getInventory().getItem(0);
        ItemStack item2 = event.getInventory().getItem(1);

        if (item1 == null || item2 == null) return;

        Sphere s1 = plugin.getSphereManager().getSphere(item1);
        Sphere s2 = plugin.getSphereManager().getSphere(item2);

        if (s1 != null && s2 != null && s1.getId().equals(s2.getId())) {
            String nextId = plugin.getSphereManager().getNextTierId(s1.getId());
            if (nextId != null) {
                Sphere next = plugin.getSphereManager().getSphere(nextId);
                if (next != null) {
                    event.setResult(next.getItemStack());
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                         @SuppressWarnings("deprecation")
                         boolean ignore = true; // Placeholder for suppressor
                         event.getInventory().setRepairCost(30);
                    });
                }
            }
        }
    }
}
