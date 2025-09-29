package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand implements SubCommand {

    private final AtheriumEnchants plugin;

    public GiveCommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "Gives a player a custom enchanted book.";
    }

    @Override
    public String getSyntax() {
        return "/ae give <player> <enchant> [level]";
    }

    @Override
    public String getPermission() {
        return "atheriumenchants.give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }

        String enchantName = args[2];
        CustomEnchant enchant = plugin.getEnchantmentManager().getEnchantByName(enchantName);
        if (enchant == null) {
            sender.sendMessage(ChatColor.RED + "Enchantment not found: " + enchantName);
            return;
        }

        int level = 1;
        if (args.length > 3) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[3]);
                return;
            }
        }

        if (level <= 0 || level > enchant.getMaxLevel()) {
            sender.sendMessage(ChatColor.RED + "Invalid level. Must be between 1 and " + enchant.getMaxLevel());
            return;
        }

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        enchant.applyToItem(book, level);

        target.getInventory().addItem(book);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a book with " + enchant.getDisplayName() + " " + level);
        target.sendMessage(ChatColor.GREEN + "You have received a book with " + enchant.getDisplayName() + " " + level);
    }
}