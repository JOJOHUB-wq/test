package com.customenchants.commands;

import com.customenchants.AtheriumEnchants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements SubCommand {

    private final AtheriumEnchants plugin;

    public MenuCommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public String getDescription() {
        return "Opens the main enchantments menu.";
    }

    @Override
    public String getSyntax() {
        return "/ae menu";
    }

    @Override
    public String getPermission() {
        return "atheriumenchants.menu"; // Let's add a permission for this
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        plugin.getMenuManager().openMenu(player, "enchants");
    }
}