package ua.atherium;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {
    private final String url;
    private final Logger logger;
    private Connection connection;

    public DatabaseManager(File dataFolder, Logger logger) {
        this.logger = logger;
        this.url = "jdbc:sqlite:" + new File(dataFolder, "data.db").getAbsolutePath();
        initialize();
    }

    private void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            createTables();
        } catch (Exception e) {
            logger.severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "telegram_id BIGINT UNIQUE, " +
                    "lang VARCHAR(5) DEFAULT 'ru', " +
                    "notifications BOOLEAN DEFAULT 1, " +
                    "autostat BOOLEAN DEFAULT 0, " +
                    "graph_color VARCHAR(7) DEFAULT '#3498db', " +
                    "graph_bg TEXT, " +
                    "signature TEXT DEFAULT '')");

            stmt.execute("CREATE TABLE IF NOT EXISTS servers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ip VARCHAR(255), " +
                    "port INTEGER DEFAULT 25565, " +
                    "owner_telegram_id BIGINT, " +
                    "alias VARCHAR(50), " +
                    "added_timestamp BIGINT, " +
                    "motd TEXT, " +
                    "favicon TEXT, " +
                    "last_version VARCHAR(50), " +
                    "is_global BOOLEAN DEFAULT 0, " +
                    "UNIQUE(ip, port))");

            stmt.execute("CREATE TABLE IF NOT EXISTS stats (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "server_id INTEGER, " +
                    "timestamp BIGINT, " +
                    "online INTEGER, " +
                    "max_players INTEGER, " +
                    "version TEXT, " +
                    "FOREIGN KEY(server_id) REFERENCES servers(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS server_records (" +
                    "server_id INTEGER PRIMARY KEY, " +
                    "all_time_record INTEGER DEFAULT 0, " +
                    "all_time_record_timestamp BIGINT, " +
                    "FOREIGN KEY(server_id) REFERENCES servers(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS scanner_blacklist (" +
                    "ip VARCHAR(255) PRIMARY KEY, " +
                    "added_timestamp BIGINT, " +
                    "expires_timestamp BIGINT)");
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createUser(long telegramId) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO users (telegram_id) VALUES (?)")) {
            ps.setLong(1, telegramId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserLang(long telegramId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT lang FROM users WHERE telegram_id = ?")) {
            ps.setLong(1, telegramId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("lang");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "ru";
    }

    public void setUserLang(long telegramId, String lang) {
        createUser(telegramId);
        try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET lang = ? WHERE telegram_id = ?")) {
            ps.setString(1, lang);
            ps.setLong(2, telegramId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserSettings getUserSettings(long telegramId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE telegram_id = ?")) {
            ps.setLong(1, telegramId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserSettings(
                        rs.getString("lang"),
                        rs.getBoolean("notifications"),
                        rs.getBoolean("autostat"),
                        rs.getString("graph_color"),
                        rs.getString("graph_bg"),
                        rs.getString("signature")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createUser(telegramId);
        return new UserSettings("ru", true, false, "#3498db", null, "");
    }

    public void updateUserSettings(long telegramId, String column, Object value) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET " + column + " = ? WHERE telegram_id = ?")) {
            ps.setObject(1, value);
            ps.setLong(2, telegramId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addServer(String ip, int port, long ownerId, String alias, boolean isGlobal) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO servers (ip, port, owner_telegram_id, alias, added_timestamp, is_global) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, ip);
            ps.setInt(2, port);
            ps.setLong(3, ownerId);
            ps.setString(4, alias);
            ps.setLong(5, System.currentTimeMillis());
            ps.setBoolean(6, isGlobal);
            ps.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public void updateServerInfo(int id, String motd, String favicon, String version) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE servers SET motd = ?, favicon = ?, last_version = ? WHERE id = ?")) {
            ps.setString(1, motd);
            ps.setString(2, favicon);
            ps.setString(3, version);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeServer(long ownerId, String aliasOrIp) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM servers WHERE owner_telegram_id = ? AND (alias = ? OR ip = ?)")) {
            ps.setLong(1, ownerId);
            ps.setString(2, aliasOrIp);
            ps.setString(3, aliasOrIp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAlias(long ownerId, String ip, String alias) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE servers SET alias = ? WHERE owner_telegram_id = ? AND ip = ?")) {
            ps.setString(1, alias);
            ps.setLong(2, ownerId);
            ps.setString(3, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ServerInfo> getUserServers(long ownerId) {
        List<ServerInfo> servers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM servers WHERE owner_telegram_id = ?")) {
            ps.setLong(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                servers.add(mapServer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servers;
    }

    public ServerInfo getServer(long ownerId, String aliasOrIp) {
        String sql = "SELECT * FROM servers WHERE (alias = ? OR ip = ?)";
        if (ownerId != 0) {
            sql += " AND owner_telegram_id = " + ownerId;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, aliasOrIp);
            ps.setString(2, aliasOrIp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapServer(rs);
            }
            if (ownerId != 0) {
                return getServer(0, aliasOrIp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ServerInfo getServerByIpPort(String ip, int port) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM servers WHERE ip = ? AND port = ?")) {
            ps.setString(1, ip);
            ps.setInt(2, port);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapServer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ServerInfo> getAllServers() {
        List<ServerInfo> servers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM servers")) {
            while (rs.next()) {
                servers.add(mapServer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servers;
    }

    private ServerInfo mapServer(ResultSet rs) throws SQLException {
        return new ServerInfo(
                rs.getInt("id"),
                rs.getString("ip"),
                rs.getInt("port"),
                rs.getString("alias"),
                rs.getString("motd"),
                rs.getString("favicon"),
                rs.getString("last_version"),
                rs.getBoolean("is_global")
        );
    }

    public void addStat(int serverId, int online, int max, String version) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO stats (server_id, timestamp, online, max_players, version) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, serverId);
            ps.setLong(2, System.currentTimeMillis());
            ps.setInt(3, online);
            ps.setInt(4, max);
            ps.setString(5, version);
            ps.executeUpdate();

            updateRecord(serverId, online);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRecord(int serverId, int online) {
        try {
            int currentRecord = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT all_time_record FROM server_records WHERE server_id = ?")) {
                ps.setInt(1, serverId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentRecord = rs.getInt("all_time_record");
                } else {
                     try (PreparedStatement insert = connection.prepareStatement("INSERT INTO server_records (server_id, all_time_record, all_time_record_timestamp) VALUES (?, 0, 0)")) {
                        insert.setInt(1, serverId);
                        insert.executeUpdate();
                     }
                }
            }

            if (online > currentRecord) {
                try (PreparedStatement ps = connection.prepareStatement("UPDATE server_records SET all_time_record = ?, all_time_record_timestamp = ? WHERE server_id = ?")) {
                    ps.setInt(1, online);
                    ps.setLong(2, System.currentTimeMillis());
                    ps.setInt(3, serverId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RecordInfo getRecord(int serverId) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM server_records WHERE server_id = ?")) {
            ps.setInt(1, serverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new RecordInfo(rs.getInt("all_time_record"), rs.getLong("all_time_record_timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RecordInfo(0, 0);
    }

    public List<StatPoint> getStats(int serverId, long startTimestamp) {
        List<StatPoint> stats = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM stats WHERE server_id = ? AND timestamp >= ? ORDER BY timestamp ASC")) {
            ps.setInt(1, serverId);
            ps.setLong(2, startTimestamp);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stats.add(new StatPoint(
                        rs.getLong("timestamp"),
                        rs.getInt("online")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<TopServer> getTopServers(int limit, int offset) {
        List<TopServer> top = new ArrayList<>();
        String sql = "SELECT s.ip, s.alias, st.online FROM servers s " +
                     "JOIN stats st ON s.id = st.server_id " +
                     "WHERE st.timestamp = (SELECT MAX(timestamp) FROM stats WHERE server_id = s.id) " +
                     "ORDER BY st.online DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("alias");
                if (name == null || name.isEmpty()) name = rs.getString("ip");
                top.add(new TopServer(name, rs.getInt("online")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    public void addToBlacklist(String ip, long durationMs) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO scanner_blacklist (ip, added_timestamp, expires_timestamp) VALUES (?, ?, ?)")) {
            long now = System.currentTimeMillis();
            ps.setString(1, ip);
            ps.setLong(2, now);
            ps.setLong(3, now + durationMs);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBlacklisted(String ip) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT expires_timestamp FROM scanner_blacklist WHERE ip = ?")) {
            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("expires_timestamp") > System.currentTimeMillis();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public record UserSettings(String lang, boolean notifications, boolean autostat, String graphColor, String graphBg, String signature) {}
    public record ServerInfo(int id, String ip, int port, String alias, String motd, String favicon, String version, boolean isGlobal) {}
    public record StatPoint(long timestamp, int online) {}
    public record TopServer(String name, int online) {}
    public record RecordInfo(int online, long timestamp) {}
}
