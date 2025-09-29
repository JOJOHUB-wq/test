package com.customenchants.enchants;

import com.customenchants.AtheriumEnchants;

public class AutoSmeltEnchant extends CustomEnchant {

    public AutoSmeltEnchant(AtheriumEnchants plugin) {
        super("melting", plugin);
    }

    // The logic is now handled by the central BlockBreakHandler.
    // This class is now primarily for registration and holding enchantment properties.
}