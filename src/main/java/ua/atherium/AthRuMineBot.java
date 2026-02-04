package ua.atherium;

import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.atherium.bot.AthBot;
import ua.atherium.utils.MessageService;
import ua.atherium.utils.NetworkScanner;
import ua.atherium.utils.StatCollector;

import java.io.File;
import java.util.logging.Level;

public class AthRuMineBot extends JavaPlugin {
    private DatabaseManager db;
    private NetworkScanner scanner;
    private AthBot bot;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        db = new DatabaseManager(getDataFolder(), getLogger());

        MessageService messages = new MessageService(getDataFolder());

        try {
            String token = getConfig().getString("bot-token");
            if (token == null || token.equals("YOUR_TOKEN")) {
                getLogger().warning("Bot token not set! Please set it in config.yml");
            } else {
                bot = new AthBot(token, db, messages, getLogger());
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
                getLogger().info("Telegram Bot started successfully!");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start Telegram Bot", e);
        }

        scanner = new NetworkScanner(db, getConfig(), getLogger());
        scanner.start();

        new StatCollector(db, getLogger()).runTaskTimerAsynchronously(this, 200L, 12000L); // Delay 10s start

        getLogger().info("AthRuMineBot enabled!");
    }

    @Override
    public void onDisable() {
        if (scanner != null) {
            scanner.stop();
        }
        if (db != null) {
            db.close();
        }
        getLogger().info("AthRuMineBot disabled!");
    }
}
