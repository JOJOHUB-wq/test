package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    private final AtheriumEnchants plugin;

    public ReloadCommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin's configuration files.";
    }

    @Override
    public String getSyntax() {
        return "/ae reload";
    }

    @Override
    public String getPermission() {
        return "atheriumenchants.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getEnchantmentConfig().reloadConfig();
        plugin.getMenuManager().loadMenus();
        sender.sendMessage(ChatColor.GREEN + "AtheriumEnchants configurations have been reloaded.");
    }
}