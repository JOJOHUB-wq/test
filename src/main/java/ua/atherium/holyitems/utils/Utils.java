package ua.atherium.holyitems.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static String color(String text) {
        if (text == null) return "";
        // Support for hex colors &#RRGGBB
        // Basic legacy support
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> text) {
        return text.stream().map(Utils::color).collect(Collectors.toList());
    }
}
