package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;

public class BulldozerEnchant extends CustomEnchant {

    public BulldozerEnchant(AtheriumEnchants plugin) {
        super("bulldozer", plugin);
    }

    // The logic is now handled by the central BlockBreakHandler.
    // This class is now primarily for registration and holding enchantment properties.
}