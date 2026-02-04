package ua.atherium.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.DatabaseManager;

import java.util.List;
import java.util.logging.Logger;

public class StatCollector extends BukkitRunnable {
    private final DatabaseManager db;
    private final ServerPinger pinger;
    private final Logger logger;

    public StatCollector(DatabaseManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
        this.pinger = new ServerPinger();
    }

    @Override
    public void run() {
        List<DatabaseManager.ServerInfo> servers = db.getAllServers();
        if (servers.isEmpty()) return;

        logger.info("Starting stat collection for " + servers.size() + " servers...");
        for (DatabaseManager.ServerInfo server : servers) {

        }

        servers.parallelStream().forEach(server -> {
            try {
                String address = server.ip();
                if (server.port() != 25565) address += ":" + server.port();

                ServerPinger.PingResult result = pinger.ping(address);
                if (result.online) {
                    db.addStat(server.id(), result.players, result.maxPlayers, result.version);
                    db.updateServerInfo(server.id(), result.motd, result.favicon, result.version);
                }
            } catch (Exception e) {
            }
        });

        logger.info("Stat collection finished.");
    }
}
