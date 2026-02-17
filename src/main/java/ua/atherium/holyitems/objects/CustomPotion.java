package ua.atherium.holyitems.objects;

import org.bukkit.potion.PotionEffect;
import java.util.List;

public class CustomPotion {
    private final String id;
    private final String name;
    private final String color;
    private final List<PotionEffect> effects;
    private final List<String> lore;
    private final double price;

    public CustomPotion(String id, String name, String color, List<PotionEffect> effects, List<String> lore, double price) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.effects = effects;
        this.lore = lore;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public List<PotionEffect> getEffects() { return effects; }
    public List<String> getLore() { return lore; }
    public double getPrice() { return price; }
}
