package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantCommand implements SubCommand {

    private final AtheriumEnchants plugin;

    public EnchantCommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "enchant";
    }

    @Override
    public String getDescription() {
        return "Applies a custom enchantment to the item you are holding.";
    }

    @Override
    public String getSyntax() {
        return "/ae enchant <enchant> [level]";
    }

    @Override
    public String getPermission() {
        return "atheriumenchants.enchant";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getSyntax());
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "You must be holding an item to enchant.");
            return;
        }

        String enchantName = args[1];
        CustomEnchant enchant = plugin.getEnchantmentManager().getEnchantByName(enchantName);
        if (enchant == null) {
            sender.sendMessage(ChatColor.RED + "Enchantment not found: " + enchantName);
            return;
        }

        if (!enchant.canEnchantItem(item)) {
            sender.sendMessage(ChatColor.RED + "This enchantment cannot be applied to this item.");
            return;
        }

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[2]);
                return;
            }
        }

        if (level <= 0 || level > enchant.getMaxLevel()) {
            sender.sendMessage(ChatColor.RED + "Invalid level. Must be between 1 and " + enchant.getMaxLevel());
            return;
        }

        enchant.applyToItem(item, level);
        sender.sendMessage(ChatColor.GREEN + "Successfully applied " + enchant.getDisplayName() + " " + level + " to your item.");
    }
}