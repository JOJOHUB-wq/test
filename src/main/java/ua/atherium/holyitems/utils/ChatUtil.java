package ua.atherium.holyitems.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> color(List<String> messages) {
        if (messages == null) return null;
        return messages.stream().map(ChatUtil::color).collect(Collectors.toList());
    }
}
