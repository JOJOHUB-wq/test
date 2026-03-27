package ua.atherium.holyitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.guis.SphereTraderGUI;
import ua.atherium.holyitems.utils.ChatUtil;

public class SpheresCommand implements CommandExecutor {
    private final HolyWorldItems plugin;
    public SpheresCommand(HolyWorldItems plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can use this command."));
            return true;
        }
        new SphereTraderGUI(plugin, (Player) sender).open();
        return true;
    }
}
