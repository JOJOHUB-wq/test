package ua.atherium.holyitems.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HolyItemsCommand implements CommandExecutor, TabCompleter {

    private final HolyWorldItems plugin;

    public HolyItemsCommand(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openMainMenu((Player) sender);
            } else {
                sender.sendMessage(Utils.color("&cТолько игрок может открыть GUI."));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                if (!sender.hasPermission("holyitems.admin")) {
                    sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.no_permission")));
                    return true;
                }
                plugin.getConfigManager().reloadConfigs();
                plugin.getItemManager().loadItems();
                plugin.getSphereManager().loadSpheres();
                plugin.getEnchantmentManager().loadEnchantments();
                plugin.getPotionManager().loadPotions();
                sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.reload_success")));
                break;

            case "give":
                if (!sender.hasPermission("holyitems.admin")) {
                    sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.no_permission")));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Utils.color("&cИспользование: /holyitems give <предмет> [игрок] [количество]"));
                    return true;
                }
                String itemId = args[1];
                Player target = (sender instanceof Player) ? (Player) sender : null;
                if (args.length >= 3) {
                    target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(Utils.color("&cИгрок не найден."));
                        return true;
                    }
                }
                if (target == null) {
                    sender.sendMessage(Utils.color("&cУкажите игрока."));
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Utils.color("&cНеверное количество."));
                        return true;
                    }
                }

                ItemStack item = plugin.getItemManager().getItem(itemId);
                if (item == null) {
                    // Try Sphere
                    item = plugin.getSphereManager().getSphere(itemId);
                }
                if (item == null) {
                    // Try Potion
                    item = plugin.getPotionManager().getPotion(itemId);
                }

                if (item == null) {
                    sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.item_not_found").replace("{item}", itemId)));
                    return true;
                }

                item.setAmount(amount);
                target.getInventory().addItem(item);
                sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.give_success")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{item}", itemId)
                        .replace("{player}", target.getName())));
                target.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.receive_item")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{item}", itemId))); // Better name?
                break;

            case "list":
                if (!sender.hasPermission("holyitems.admin")) {
                    sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.no_permission")));
                    return true;
                }
                sender.sendMessage(Utils.color("&6Список предметов:"));
                sender.sendMessage(Utils.color("&eItems: &f" + String.join(", ", plugin.getItemManager().getItems().keySet())));
                sender.sendMessage(Utils.color("&eSpheres: &f" + String.join(", ", plugin.getSphereManager().getSpheres().keySet())));
                sender.sendMessage(Utils.color("&ePotions: &f" + String.join(", ", plugin.getPotionManager().getPotions().keySet())));
                break;

            case "create":
                if (!sender.hasPermission("holyitems.admin")) {
                    sender.sendMessage(Utils.color(plugin.getConfigManager().getConfig("config.yml").getString("messages.no_permission")));
                    return true;
                }
                if (sender instanceof Player) {
                    plugin.getGuiManager().openCreator((Player) sender);
                }
                break;

            default:
                sender.sendMessage(Utils.color("&cНеизвестная подкоманда."));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("reload");
            subs.add("give");
            subs.add("list");
            subs.add("create");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> items = new ArrayList<>(plugin.getItemManager().getItems().keySet());
            items.addAll(plugin.getSphereManager().getSpheres().keySet());
            items.addAll(plugin.getPotionManager().getPotions().keySet());
            return items.stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return null; // Players
        }
        return new ArrayList<>();
    }
}
