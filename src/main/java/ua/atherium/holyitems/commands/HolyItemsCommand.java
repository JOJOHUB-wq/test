package ua.atherium.holyitems.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.guis.ItemCreatorGUI;
import ua.atherium.holyitems.guis.ItemListGUI;
import ua.atherium.holyitems.guis.MainMenuGUI;
import ua.atherium.holyitems.objects.CustomItem;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
                new MainMenuGUI(plugin, (Player) sender).open();
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("holyitems.reload")) {
                sender.sendMessage(ChatUtil.color("&cНет прав!"));
                return true;
            }
            plugin.getConfigManager().reloadAll();
            plugin.getItemManager().loadItems();
            plugin.getSphereManager().loadSpheres();
            plugin.getEnchantmentManager().loadEnchantments();
            plugin.getPotionManager().loadPotions();
            sender.sendMessage(ChatUtil.color("&aКонфигурация перезагружена!"));
            return true;
        }

        if (sub.equals("give")) {
            if (!sender.hasPermission("holyitems.give")) {
                sender.sendMessage(ChatUtil.color("&cНет прав!"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatUtil.color("&cИспользование: /holyitems give <item> [player] [amount]"));
                return true;
            }

            String itemId = args[1];
            Player target = (sender instanceof Player) ? (Player) sender : null;
            int amount = 1;

            if (args.length >= 3) {
                target = Bukkit.getPlayer(args[2]);
            }
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatUtil.color("&cНеверное количество!"));
                    return true;
                }
            }

            if (target == null) {
                sender.sendMessage(ChatUtil.color("&cИгрок не найден!"));
                return true;
            }

            // Check Items
            CustomItem item = plugin.getItemManager().getItem(itemId);
            if (item != null) {
                plugin.getItemManager().giveItem(target, itemId, amount);
                sender.sendMessage(ChatUtil.color("&aВыдано " + amount + "x " + itemId + " игроку " + target.getName()));
                return true;
            }

            // Check Spheres
            if (plugin.getSphereManager().getSphere(itemId) != null) {
                target.getInventory().addItem(plugin.getSphereManager().getSphere(itemId).getItemStack());
                sender.sendMessage(ChatUtil.color("&aВыдана сфера " + itemId));
                return true;
            }

            sender.sendMessage(ChatUtil.color("&cПредмет не найден: " + itemId));
            return true;
        }

        if (sub.equals("list")) {
            if (!(sender instanceof Player)) return true;
            String category = args.length > 1 ? args[1].toUpperCase() : null;
            if (category != null) {
                new ItemListGUI(plugin, (Player) sender, category).open();
            } else {
                new MainMenuGUI(plugin, (Player) sender).open();
            }
            return true;
        }

        if (sub.equals("create")) {
            if (!sender.hasPermission("holyitems.admin")) {
                sender.sendMessage(ChatUtil.color("&cНет прав!"));
                return true;
            }
            if (!(sender instanceof Player)) return true;
            new ItemCreatorGUI(plugin, (Player) sender).open();
            return true;
        }

        if (sub.equals("debug")) {
            if (!sender.hasPermission("holyitems.debug")) {
                sender.sendMessage(ChatUtil.color("&cНет прав!"));
                return true;
            }
            Player target = (sender instanceof Player) ? (Player) sender : null;
            if (args.length > 1) {
                target = Bukkit.getPlayer(args[1]);
            }

            if (target != null) {
                boolean current = plugin.getCooldownManager().isDebug(target);
                plugin.getCooldownManager().setDebug(target, !current);
                sender.sendMessage(ChatUtil.color("&aРежим отладки для " + target.getName() + ": " + (!current ? "&aВКЛ" : "&cВЫКЛ")));
            } else {
                sender.sendMessage(ChatUtil.color("&cИгрок не найден!"));
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtil.color("&6HolyItems Help:"));
        sender.sendMessage(ChatUtil.color("&e/holyitems give <item> [player] [amount]"));
        sender.sendMessage(ChatUtil.color("&e/holyitems list [category]"));
        sender.sendMessage(ChatUtil.color("&e/holyitems reload"));
        sender.sendMessage(ChatUtil.color("&e/holyitems create"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("give", "list", "reload", "create", "info", "debug"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> items = plugin.getItemManager().getAllItems().stream().map(CustomItem::getId).collect(Collectors.toList());
            items.addAll(plugin.getSphereManager().getAllSpheres().stream().map(s -> s.getId()).collect(Collectors.toList()));
            return filter(items, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return null; // Players
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        if (input == null || input.isEmpty()) return list;
        return list.stream().filter(s -> s.toLowerCase().startsWith(input.toLowerCase())).collect(Collectors.toList());
    }
}
