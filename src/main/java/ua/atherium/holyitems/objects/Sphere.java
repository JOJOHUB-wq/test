package ua.atherium.holyitems.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import java.util.List;

public class Sphere {
    private final String id;
    private final Rarity rarity;
    private final SphereType type;
    private final ItemStack itemStack;
    private final List<SphereEffect> effects;
    private final List<PotionEffect> potionEffects;
    private final int convertCost;
    private final int shardValue;
    private final boolean convertible;

    public Sphere(String id, Rarity rarity, SphereType type, ItemStack itemStack, List<SphereEffect> effects, List<PotionEffect> potionEffects, int convertCost, int shardValue, boolean convertible) {
        this.id = id;
        this.rarity = rarity;
        this.type = type;
        this.itemStack = itemStack;
        this.effects = effects;
        this.potionEffects = potionEffects;
        this.convertCost = convertCost;
        this.shardValue = shardValue;
        this.convertible = convertible;
    }

    public String getId() { return id; }
    public Rarity getRarity() { return rarity; }
    public SphereType getType() { return type; }
    public ItemStack getItemStack() { return itemStack.clone(); }
    public List<SphereEffect> getEffects() { return effects; }
    public List<PotionEffect> getPotionEffects() { return potionEffects; }
    public int getConvertCost() { return convertCost; }
    public int getShardValue() { return shardValue; }
    public boolean isConvertible() { return convertible; }
}
