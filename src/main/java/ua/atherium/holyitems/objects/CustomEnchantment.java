package ua.atherium.holyitems.objects;

import java.util.List;
import java.util.Map;

public class CustomEnchantment {
    private final String id;
    private final String name;
    private final int maxLevel;
    private final List<String> appliesTo;
    private final String description;
    private final Map<String, Object> effect;
    private final String vanillaEnchant;

    public CustomEnchantment(String id, String name, int maxLevel, List<String> appliesTo, String description, Map<String, Object> effect, String vanillaEnchant) {
        this.id = id;
        this.name = name;
        this.maxLevel = maxLevel;
        this.appliesTo = appliesTo;
        this.description = description;
        this.effect = effect;
        this.vanillaEnchant = vanillaEnchant;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxLevel() { return maxLevel; }
    public List<String> getAppliesTo() { return appliesTo; }
    public String getDescription() { return description; }
    public Map<String, Object> getEffect() { return effect; }
    public String getVanillaEnchant() { return vanillaEnchant; }
    public boolean isVanilla() { return vanillaEnchant != null; }
}
