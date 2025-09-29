package com.customenchants.commands;

import com.customenchants.CustomEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveEnchantCommand implements CommandExecutor {

    private final CustomEnchants plugin;

    public GiveEnchantCommand(CustomEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customenchants.give")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /giveenchant <player> <enchant_name> [level]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        String enchantName = args[1];
        CustomEnchant enchant = plugin.getEnchantmentManager().getEnchantByName(enchantName);
        if (enchant == null) {
            sender.sendMessage(ChatColor.RED + "Enchantment not found: " + enchantName);
            return true;
        }

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[2]);
                return true;
            }
        }

        if (level <= 0 || level > enchant.getMaxLevel()) {
            sender.sendMessage(ChatColor.RED + "Invalid level. Must be between 1 and " + enchant.getMaxLevel());
            return true;
        }

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        enchant.applyToItem(book, level);

        target.getInventory().addItem(book);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a book with " + enchant.getName() + " " + level);
        target.sendMessage(ChatColor.GREEN + "You have received a book with " + enchant.getName() + " " + level);

        return true;
    }
}