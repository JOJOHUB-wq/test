package ua.atherium.holyitems.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SetFilterCommand implements CommandExecutor {

    private final HolyWorldItems plugin;

    public SetFilterCommand(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cТолько игрок."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open GUI (Placeholder logic for now, using command args as fallback/primary)
            // Ideally open GUI.
            // plugin.getGuiManager().openFilterGUI(player);
            // Since GUI logic is extensive, let's provide command usage first.
            sender.sendMessage(Utils.color("&6Фильтр предметов:"));
            sender.sendMessage(Utils.color("&e/setfilter add &f- Добавить предмет в руке"));
            sender.sendMessage(Utils.color("&e/setfilter remove &f- Удалить предмет в руке"));
            sender.sendMessage(Utils.color("&e/setfilter list &f- Список"));
            sender.sendMessage(Utils.color("&e/setfilter clear &f- Очистить"));
            return true;
        }

        String sub = args[0].toLowerCase();
        List<Material> filter = plugin.getPlayerDataManager().getFilter(player);

        switch (sub) {
            case "add":
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType() == Material.AIR) {
                    player.sendMessage(Utils.color("&cВозьмите предмет в руку."));
                    return true;
                }
                if (!filter.contains(hand.getType())) {
                    filter.add(hand.getType());
                    plugin.getPlayerDataManager().setFilter(player, filter);
                    player.sendMessage(Utils.color("&aПредмет " + hand.getType().name() + " добавлен в фильтр."));
                } else {
                    player.sendMessage(Utils.color("&cУже в фильтре."));
                }
                break;

            case "remove":
                ItemStack hand2 = player.getInventory().getItemInMainHand();
                if (hand2 == null || hand2.getType() == Material.AIR) {
                     // Maybe remove by name argument?
                     if (args.length > 1) {
                         Material m = Material.getMaterial(args[1].toUpperCase());
                         if (m != null && filter.remove(m)) {
                             plugin.getPlayerDataManager().setFilter(player, filter);
                             player.sendMessage(Utils.color("&aУдалено: " + m.name()));
                             return true;
                         }
                     }
                     player.sendMessage(Utils.color("&cВозьмите предмет или укажите имя."));
                     return true;
                }
                if (filter.remove(hand2.getType())) {
                    plugin.getPlayerDataManager().setFilter(player, filter);
                    player.sendMessage(Utils.color("&aПредмет " + hand2.getType().name() + " удален из фильтра."));
                } else {
                    player.sendMessage(Utils.color("&cНе найдено в фильтре."));
                }
                break;

            case "list":
                player.sendMessage(Utils.color("&6Ваш фильтр:"));
                List<String> names = new ArrayList<>();
                for (Material m : filter) names.add(m.name());
                player.sendMessage(Utils.color("&7" + String.join(", ", names)));
                break;

            case "clear":
                filter.clear();
                plugin.getPlayerDataManager().setFilter(player, filter);
                player.sendMessage(Utils.color("&aФильтр очищен."));
                break;
        }

        return true;
    }
}
