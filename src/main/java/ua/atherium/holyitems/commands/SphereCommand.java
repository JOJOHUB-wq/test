package ua.atherium.holyitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

public class SphereCommand implements CommandExecutor {

    private final HolyWorldItems plugin;

    public SphereCommand(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cТолько игрок может использовать эту команду."));
            return true;
        }

        Player player = (Player) sender;
        plugin.getGuiManager().openSphereTrader(player);
        return true;
    }
}
