package ua.atherium.holyitems.objects;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public class SphereEffect {
    private final Attribute attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    public SphereEffect(Attribute attribute, double amount, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    public Attribute getAttribute() { return attribute; }
    public double getAmount() { return amount; }
    public AttributeModifier.Operation getOperation() { return operation; }
}
