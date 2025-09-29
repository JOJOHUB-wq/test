package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final AtheriumEnchants plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        registerSubCommand(new MenuCommand(plugin));
        registerSubCommand(new GiveCommand(plugin));
        registerSubCommand(new EnchantCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help message
            sender.sendMessage(ChatColor.GREEN + "--- AtheriumEnchants Help ---");
            for (SubCommand subCommand : subCommands.values()) {
                sender.sendMessage(ChatColor.YELLOW + subCommand.getSyntax() + " - " + subCommand.getDescription());
            }
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Unknown command. Use /ae for help.");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }
}