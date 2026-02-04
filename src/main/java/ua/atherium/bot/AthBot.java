package ua.atherium.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.atherium.DatabaseManager;
import ua.atherium.utils.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.Base64;

public class AthBot extends TelegramLongPollingBot {
    private final String botToken;
    private final DatabaseManager db;
    private final MessageService messages;
    private final Logger logger;
    private final ServerPinger pinger = new ServerPinger();
    private final GraphGenerator graphGen = new GraphGenerator();
    private final QuoteGenerator quoteGen = new QuoteGenerator();
    private final Map<String, String> commandAliases = new HashMap<>();

    public AthBot(String token, DatabaseManager db, MessageService messages, Logger logger) {
        super(token);
        this.botToken = token;
        this.db = db;
        this.messages = messages;
        this.logger = logger;
        initializeAliases();
    }

    private void initializeAliases() {
        commandAliases.put("добавить", "add");
        commandAliases.put("удалить", "delete");
        commandAliases.put("пинг", "ping");
        commandAliases.put("алиас", "alias");
        commandAliases.put("стата", "stats");
        commandAliases.put("статистика", "stats");
        commandAliases.put("топ", "top");
        commandAliases.put("автодоб", "autoadd");
        commandAliases.put("автодобавление", "autoadd");
        commandAliases.put("уведомления", "notify");
        commandAliases.put("автостата", "autostat");
        commandAliases.put("автостатистика", "autostat");
        commandAliases.put("цитата", "quote");
        commandAliases.put("неделя", "week");
        commandAliases.put("сумма", "sum");
        commandAliases.put("игроки", "players");
        commandAliases.put("сравнить", "compare");
        commandAliases.put("график", "graph");
        commandAliases.put("подпись", "signature");
        commandAliases.put("язык", "lang");
        commandAliases.put("помощь", "help");
        commandAliases.put("инфо", "info");
        commandAliases.put("info", "info");

        commandAliases.put("додати", "add");
        commandAliases.put("видалити", "delete");
        commandAliases.put("пінг", "ping");
        commandAliases.put("аліас", "alias");
        commandAliases.put("сповіщення", "notify");
        commandAliases.put("тиждень", "week");
        commandAliases.put("сума", "sum");
        commandAliases.put("гравці", "players");
        commandAliases.put("порівняти", "compare");
        commandAliases.put("графік", "graph");
        commandAliases.put("підпис", "signature");
        commandAliases.put("мова", "lang");
        commandAliases.put("допомога", "help");
        commandAliases.put("інфо", "info");

        commandAliases.put("add", "add");
        commandAliases.put("delete", "delete");
        commandAliases.put("ping", "ping");
        commandAliases.put("alias", "alias");
        commandAliases.put("stats", "stats");
        commandAliases.put("top", "top");
        commandAliases.put("autoadd", "autoadd");
        commandAliases.put("notify", "notify");
        commandAliases.put("autostat", "autostat");
        commandAliases.put("quote", "quote");
        commandAliases.put("week", "week");
        commandAliases.put("sum", "sum");
        commandAliases.put("players", "players");
        commandAliases.put("compare", "compare");
        commandAliases.put("graph", "graph");
        commandAliases.put("signature", "signature");
        commandAliases.put("lang", "lang");
        commandAliases.put("help", "help");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message message = update.getMessage();
        String text = message.getText().trim();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();

        DatabaseManager.UserSettings settings = db.getUserSettings(userId);
        String lang = settings.lang();

        String[] parts = text.split("\\s+", 2);
        String rawCommand = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        String command = commandAliases.getOrDefault(rawCommand, rawCommand);

        try {
            switch (command) {
                case "add": handleAdd(chatId, userId, args, lang); break;
                case "delete": handleDelete(chatId, userId, args, lang); break;
                case "ping": handlePing(chatId, args, lang); break;
                case "info": handleInfo(chatId, userId, args, lang); break;
                case "alias": handleAlias(chatId, userId, args, lang); break;
                case "stats": handleStats(chatId, userId, args, lang); break;
                case "top": handleTop(chatId, args, lang); break;
                case "autoadd": handleAutoAdd(chatId, lang); break;
                case "notify": handleNotify(chatId, userId, args, lang); break;
                case "autostat": handleAutoStat(chatId, userId, args, lang); break;
                case "quote": handleQuote(chatId, userId, message, args, lang, settings); break;
                case "week": handleWeek(chatId, userId, args, lang); break;
                case "sum": handleSum(chatId, userId, lang); break;
                case "players": handlePlayers(chatId, args, lang); break;
                case "compare": handleCompare(chatId, userId, args, lang); break;
                case "graph": handleGraph(chatId, userId, args, lang); break;
                case "signature": handleSignature(chatId, userId, args, lang); break;
                case "lang": handleLang(chatId, userId, args, lang); break;
                case "help": handleHelp(chatId, lang); break;
                case "start": sendText(chatId, messages.get(lang, "welcome_message")); break;
                default: break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendText(chatId, "Error: " + e.getMessage());
        }
    }

    public void sendText(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText(text);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(long chatId, File photo, String caption) {
        SendPhoto sp = new SendPhoto();
        sp.setChatId(chatId);
        sp.setPhoto(new InputFile(photo));
        if (caption != null) sp.setCaption(caption);
        try {
            execute(sp);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "AthRuMineBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void handleAdd(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "add funtime.su"));
            return;
        }
        String ip = args;
        int port = 25565;
        if (args.contains(":")) {
            String[] parts = args.split(":");
            ip = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                sendText(chatId, messages.get(lang, "error_invalid_format", "add 1.2.3.4:25565"));
                return;
            }
        }

        DatabaseManager.ServerInfo existing = db.getServer(userId, args);
        if (existing != null) {
            sendText(chatId, messages.get(lang, "server_already_exists"));
            return;
        }

        DatabaseManager.ServerInfo global = db.getServer(0, args);
        if (global != null) {
            sendText(chatId, "Server is already monitored globally. You can use 'stats " + args + "'.");
            return;
        }

        db.addServer(ip, port, userId, args, false);
        sendText(chatId, messages.get(lang, "server_added", args));
    }

    private void handleDelete(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "delete <ip|alias>"));
            return;
        }
        db.removeServer(userId, args);
        sendText(chatId, messages.get(lang, "server_removed", args));
    }

    private void handlePing(long chatId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "ping <ip>"));
            return;
        }
        ServerPinger.PingResult result = pinger.ping(args);
        if (result.online) {
            sendText(chatId, messages.get(lang, "ping_online",
                args, result.players, result.maxPlayers, result.version, result.latency));
        } else {
            sendText(chatId, messages.get(lang, "ping_offline", args, result.error != null ? result.error : "Unreachable"));
        }
    }

    private void handleInfo(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "info <ip>"));
            return;
        }

        ServerPinger.PingResult result = pinger.ping(args);
        if (!result.online) {
            sendText(chatId, messages.get(lang, "ping_offline", args, result.error != null ? result.error : "Unreachable"));
            return;
        }

        String caption = String.format("🎮 Информація про сервер %s\n\n📝 MOTD: %s\n📊 Онлайн: %d/%d\n🎮 Версія: %s\n⏱ Пінг: %dms\n🔗 IP: %s:%d",
                args,
                result.motd != null ? result.motd : "N/A",
                result.players, result.maxPlayers,
                result.version,
                result.latency,
                result.ip, result.port
        );

        if (result.favicon != null && result.favicon.startsWith("data:image/png;base64,")) {
            try {
                String base64 = result.favicon.substring("data:image/png;base64,".length());
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                File temp = File.createTempFile("favicon", ".png");
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                ImageIO.write(img, "png", temp);

                sendPhoto(chatId, temp, caption);
                temp.delete();
                return;
            } catch (Exception e) {
            }
        }
        sendText(chatId, caption);
    }

    private void handleAlias(long chatId, long userId, String args, String lang) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "alias <ip> <name>"));
            return;
        }
        String ip = parts[0];
        String alias = parts[1];
        db.setAlias(userId, ip, alias);
        sendText(chatId, messages.get(lang, "alias_set", alias, ip));
    }

    private void handleStats(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
             List<DatabaseManager.ServerInfo> servers = db.getUserServers(userId);
             if (servers.isEmpty()) {
                 sendText(chatId, messages.get(lang, "server_not_found"));
                 return;
             }
             StringBuilder sb = new StringBuilder("🖥 Your Servers:\n");
             for (DatabaseManager.ServerInfo s : servers) {
                 sb.append("- ").append(s.alias()).append(" (").append(s.ip()).append(")\n");
             }
             sendText(chatId, sb.toString());
             return;
        }

        DatabaseManager.ServerInfo server = db.getServer(userId, args);
        if (server == null) {
            sendText(chatId, messages.get(lang, "server_not_found"));
            return;
        }

        long OneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
        List<DatabaseManager.StatPoint> stats = db.getStats(server.id(), OneDayAgo);

        if (stats.isEmpty()) {
             sendText(chatId, "No stats available yet.");
             return;
        }

        DatabaseManager.UserSettings settings = db.getUserSettings(userId);
        File graph = graphGen.generateStatsGraph(
                server.alias(),
                stats,
                settings.graphColor(),
                settings.graphBg()
        );

        int current = stats.get(stats.size() - 1).online();
        int min = stats.stream().mapToInt(DatabaseManager.StatPoint::online).min().orElse(0);
        int max = stats.stream().mapToInt(DatabaseManager.StatPoint::online).max().orElse(0);
        double avg = stats.stream().mapToInt(DatabaseManager.StatPoint::online).average().orElse(0);

        int ago24h = stats.get(0).online();

        DatabaseManager.RecordInfo record = db.getRecord(server.id());

        String caption = String.format("📊 Статистика сервера %s:\n\n– Текущий онлайн: %d\n– Онлайн сутки назад: %d\n– Минимальный онлайн за сутки: %d\n– Средний онлайн за сутки: %.0f\n– Рекорд онлайна за сутки: %d\n– Рекорд онлайна за всё время: %d",
                server.alias(), current, ago24h, min, avg, max, record.online());

        if (graph != null) {
            sendPhoto(chatId, graph, caption);
            graph.delete();
        } else {
            sendText(chatId, caption);
        }
    }

    private void handleTop(long chatId, String args, String lang) {
        int page = 1;
        try {
            if (!args.isEmpty()) page = Integer.parseInt(args);
        } catch (NumberFormatException ignored) {}
        if (page < 1) page = 1;

        List<DatabaseManager.TopServer> top = db.getTopServers(10, (page - 1) * 10);
        if (top.isEmpty()) {
            sendText(chatId, "No servers found.");
            return;
        }

        StringBuilder sb = new StringBuilder(messages.get(lang, "top_header", page)).append("\n\n");
        int rank = (page - 1) * 10 + 1;
        for (DatabaseManager.TopServer s : top) {
            sb.append(messages.get(lang, "top_entry", rank++, s.name(), s.online())).append("\n");
        }
        sendText(chatId, sb.toString());
    }

    private void handleAutoAdd(long chatId, String lang) {
        sendText(chatId, "🔥 Popular Servers:\n- funtime.su\n- holyworld.ru\n- reallyworld.ru\n- vimeworld.com\n- hypixel.net");
    }

    private void handleNotify(long chatId, long userId, String args, String lang) {
        boolean enable = true;
        if (args.equalsIgnoreCase("выкл") || args.equalsIgnoreCase("off") || args.equals("0")) enable = false;

        db.updateUserSettings(userId, "notifications", enable);
        sendText(chatId, messages.get(lang, enable ? "notify_on" : "notify_off"));
    }

    private void handleAutoStat(long chatId, long userId, String args, String lang) {
        boolean enable = true;
        if (args.equalsIgnoreCase("выкл") || args.equalsIgnoreCase("off") || args.equals("0")) enable = false;

        db.updateUserSettings(userId, "autostat", enable);
        sendText(chatId, messages.get(lang, enable ? "autostat_on" : "autostat_off"));
    }

    private void handleQuote(long chatId, long userId, Message message, String args, String lang, DatabaseManager.UserSettings settings) {
        if (!message.isReply()) {
            sendText(chatId, "Reply to a message to quote it.");
            return;
        }
        Message reply = message.getReplyToMessage();
        if (!reply.hasText()) {
            sendText(chatId, "Reply to a text message.");
            return;
        }

        String text = reply.getText();
        String username = reply.getFrom().getFirstName();
        if (reply.getFrom().getLastName() != null) username += " " + reply.getFrom().getLastName();

        BufferedImage avatar = null;
        try {
            List<PhotoSize> photos = execute(new org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos(reply.getFrom().getId())).getPhotos().get(0);
            if (!photos.isEmpty()) {
                PhotoSize photo = photos.get(photos.size() - 1);
                GetFile getFile = new GetFile(photo.getFileId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                String url = file.getFileUrl(getBotToken());
                avatar = ImageIO.read(new URL(url));
            }
        } catch (Exception e) {
        }

        File quote = quoteGen.generateQuote(avatar, username, text, settings.signature());
        if (quote != null) {
            sendPhoto(chatId, quote, null);
            quote.delete();
        } else {
            sendText(chatId, "Error generating quote.");
        }
    }

    private void handleWeek(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "week <server>"));
            return;
        }
        DatabaseManager.ServerInfo server = db.getServer(userId, args);
        if (server == null) {
            sendText(chatId, messages.get(lang, "server_not_found"));
            return;
        }

        long weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L;
        List<DatabaseManager.StatPoint> stats = db.getStats(server.id(), weekAgo);
        if (stats.isEmpty()) {
             sendText(chatId, "No stats available yet.");
             return;
        }

        DatabaseManager.UserSettings settings = db.getUserSettings(userId);
        File graph = graphGen.generateStatsGraph(
                server.alias() + " (Week)",
                stats,
                settings.graphColor(),
                settings.graphBg()
        );

        if (graph != null) {
            sendPhoto(chatId, graph, "📊 Weekly Statistics");
            graph.delete();
        }
    }

    private void handleSum(long chatId, long userId, String lang) {
        List<DatabaseManager.ServerInfo> servers = db.getUserServers(userId);
        if (servers.isEmpty()) {
            sendText(chatId, messages.get(lang, "server_not_found"));
            return;
        }

        Map<Long, Integer> agg = new TreeMap<>();
        long OneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;

        for (DatabaseManager.ServerInfo s : servers) {
            List<DatabaseManager.StatPoint> stats = db.getStats(s.id(), OneDayAgo);
            for (DatabaseManager.StatPoint p : stats) {
                long key = (p.timestamp() / 600000) * 600000;
                agg.merge(key, p.online(), Integer::sum);
            }
        }

        List<DatabaseManager.StatPoint> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : agg.entrySet()) {
            result.add(new DatabaseManager.StatPoint(entry.getKey(), entry.getValue()));
        }

        DatabaseManager.UserSettings settings = db.getUserSettings(userId);
        File graph = graphGen.generateStatsGraph("Summary", result, settings.graphColor(), settings.graphBg());
        if (graph != null) {
            sendPhoto(chatId, graph, "📊 Summary Statistics");
            graph.delete();
        }
    }

    private void handlePlayers(long chatId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "players <ip>"));
            return;
        }

        ServerPinger.PingResult result = pinger.ping(args);
        if (result.online) {
             sendText(chatId, messages.get(lang, "players_list", args, result.players + "/" + result.maxPlayers));
        } else {
             sendText(chatId, messages.get(lang, "ping_offline", args, "Offline"));
        }
    }

    private void handleCompare(long chatId, long userId, String args, String lang) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) {
            sendText(chatId, messages.get(lang, "error_invalid_format", "compare <ip1> <ip2>"));
            return;
        }

        Map<String, List<DatabaseManager.StatPoint>> data = new HashMap<>();
        long OneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;

        List<DatabaseManager.ServerInfo> all = db.getAllServers();

        for (String target : parts) {
            DatabaseManager.ServerInfo s = all.stream()
                .filter(srv -> srv.alias().equalsIgnoreCase(target) || srv.ip().equalsIgnoreCase(target))
                .findFirst()
                .orElse(null);

            if (s != null) {
                data.put(s.alias(), db.getStats(s.id(), OneDayAgo));
            }
        }

        if (data.isEmpty()) {
            sendText(chatId, "No monitored servers found to compare.");
            return;
        }

        DatabaseManager.UserSettings settings = db.getUserSettings(userId); // Use requester's BG
        File graph = graphGen.generateCompareGraph("Comparison", data, settings.graphBg());

        if (graph != null) {
            sendPhoto(chatId, graph, "📊 Comparison");
            graph.delete();
        }
    }

    private void handleGraph(long chatId, long userId, String args, String lang) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 1) {
             sendText(chatId, messages.get(lang, "error_invalid_format", "graph <color>"));
             return;
        }

        if (parts[0].equalsIgnoreCase("фон") || parts[0].equalsIgnoreCase("bg")) {
            if (parts.length < 2) {
                 sendText(chatId, messages.get(lang, "error_invalid_format", "graph bg <url>"));
                 return;
            }
            String url = parts[1];
            db.updateUserSettings(userId, "graph_bg", url.equalsIgnoreCase("удалить") ? null : url);
            sendText(chatId, messages.get(lang, "graph_bg_changed"));
        } else {
            String color = parts[0];
            db.updateUserSettings(userId, "graph_color", color);
            sendText(chatId, messages.get(lang, "graph_color_changed"));
        }
    }

    private void handleSignature(long chatId, long userId, String args, String lang) {
        db.updateUserSettings(userId, "signature", args);
        sendText(chatId, messages.get(lang, "signature_changed"));
    }

    private void handleLang(long chatId, long userId, String args, String lang) {
        if (args.isEmpty()) {
            sendText(chatId, "Current lang: " + lang + ". Usage: lang <ru|en|ua|de|kz|by>");
            return;
        }
        String newLang = args.toLowerCase();
        if (!List.of("ru", "en", "ua", "de", "kz", "by").contains(newLang)) {
            newLang = "en";
        }
        db.setUserLang(userId, newLang);
        sendText(chatId, messages.get(newLang, "lang_changed"));
    }

    private void handleHelp(long chatId, String lang) {
        sendText(chatId, messages.get(lang, "help_text"));
    }
}
