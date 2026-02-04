package ua.atherium.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.MessageFormat;

public class MessageService {
    private final YamlConfiguration config;

    public MessageService(File dataFolder) {
        File file = new File(dataFolder, "messages.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String lang, String key, Object... args) {
        if (lang == null) lang = "ru";
        String path = lang + "." + key;
        String msg = config.getString(path);
        if (msg == null) {
            msg = config.getString("en." + key);
        }
        if (msg == null) {
            return key;
        }
        return MessageFormat.format(msg, args);
    }
}
