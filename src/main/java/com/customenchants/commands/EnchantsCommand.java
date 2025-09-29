package com.customenchants.commands;

import com.customenchants.CustomEnchants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnchantsCommand implements CommandExecutor {

    private final CustomEnchants plugin;

    public EnchantsCommand(CustomEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // The menu ID "enchants" is hardcoded for now.
        // This will correspond to the `enchants.yml` file.
        plugin.getMenuManager().openMenu(player, "enchants");
        return true;
    }
}