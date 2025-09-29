package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import com.customenchants.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AetheriumTabCompleter implements TabCompleter {

    private final AtheriumEnchants plugin;

    public AetheriumTabCompleter(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = Arrays.asList("give", "reload", "menu", "enchant");

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                // Suggest player names
                return null; // Bukkit's default player completion
            }
            if (args[0].equalsIgnoreCase("enchant")) {
                // Suggest enchantment names
                List<String> enchantNames = plugin.getEnchantmentManager().getRegisteredEnchants().stream()
                        .map(CustomEnchant::getName)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], enchantNames, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                // Suggest enchantment names
                List<String> enchantNames = plugin.getEnchantmentManager().getRegisteredEnchants().stream()
                        .map(CustomEnchant::getName)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[2], enchantNames, completions);
            }
        }

        return completions;
    }
}