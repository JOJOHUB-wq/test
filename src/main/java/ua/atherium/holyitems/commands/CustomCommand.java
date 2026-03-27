package ua.atherium.holyitems.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

public class CustomCommand implements CommandExecutor {

    private final HolyWorldItems plugin;

    public CustomCommand(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cТолько игрок может использовать эту команду."));
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            // Open GUI or show usage
            // Prompt: "/custom - Open material selector for Trap"
            // So open GUI.
            plugin.getGuiManager().openCustomMaterialSelector(player);
            return true;
        }

        // Or if used with args? Maybe /custom <material>
        String matName = args[0].toUpperCase();
        Material mat = Material.getMaterial(matName);
        if (mat == null || !mat.isBlock() || mat == Material.AIR) {
            player.sendMessage(Utils.color("&cНекорректный материал."));
            return true;
        }

        plugin.getPlayerDataManager().setTrapMaterial(player, mat);
        player.sendMessage(Utils.color("&aМатериал ловушки установлен: " + mat.name()));

        return true;
    }
}
