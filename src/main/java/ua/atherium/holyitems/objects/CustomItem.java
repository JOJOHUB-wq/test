package ua.atherium.holyitems.objects;

import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class CustomItem {
    private final String id;
    private final ItemStack itemStack;
    private final long cooldown;
    private final Map<String, Object> effects;
    private final double price;
    private final boolean consume;
    private final boolean placeable;
    private final String type;

    public CustomItem(String id, ItemStack itemStack, long cooldown, Map<String, Object> effects, double price, boolean consume, boolean placeable, String type) {
        this.id = id;
        this.itemStack = itemStack;
        this.cooldown = cooldown;
        this.effects = effects;
        this.price = price;
        this.consume = consume;
        this.placeable = placeable;
        this.type = type;
    }

    public String getId() { return id; }
    public ItemStack getItemStack() { return itemStack.clone(); }
    public long getCooldown() { return cooldown; }
    public Map<String, Object> getEffects() { return effects; }
    public double getPrice() { return price; }
    public boolean isConsume() { return consume; }
    public boolean isPlaceable() { return placeable; }
    public String getType() { return type; }
}
