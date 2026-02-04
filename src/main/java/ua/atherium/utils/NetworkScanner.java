package ua.atherium.utils;

import org.bukkit.configuration.file.FileConfiguration;
import ua.atherium.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class NetworkScanner implements Runnable {
    private final DatabaseManager db;
    private final Logger logger;
    private final boolean enabled;
    private final int threads;
    private final int delayMs;
    private final List<IpRange> ranges = new ArrayList<>();
    private final Random random = new Random();
    private volatile boolean running = false;
    private final ServerPinger pinger = new ServerPinger();

    public NetworkScanner(DatabaseManager db, FileConfiguration config, Logger logger) {
        this.db = db;
        this.logger = logger;
        this.enabled = config.getBoolean("scanner.enabled", false);
        this.threads = config.getInt("scanner.threads", 1);
        int ipsPerMinute = config.getInt("scanner.ips-per-minute", 500);
        this.delayMs = ipsPerMinute > 0 ? (60000 * threads / ipsPerMinute) : 1000;

        List<String> configRanges = config.getStringList("scanner.ranges");
        for (String s : configRanges) {
            try {
                String[] parts = s.split("-");
                if (parts.length == 2) {
                    ranges.add(new IpRange(ipToLong(parts[0]), ipToLong(parts[1])));
                }
            } catch (Exception e) {
                logger.warning("Invalid IP range in config: " + s);
            }
        }
    }

    public void start() {
        if (!enabled || ranges.isEmpty()) return;
        running = true;
        for (int i = 0; i < threads; i++) {
            new Thread(this, "AthRuMineBot-Scanner-" + i).start();
        }
        logger.info("Network Scanner started with " + threads + " threads, checking " + ranges.size() + " ranges.");
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                IpRange range = ranges.get(random.nextInt(ranges.size()));
                long randomIpLong = range.start + (long) (random.nextDouble() * (range.end - range.start));
                String ip = longToIp(randomIpLong);

                if (!isPrivate(ip)) {
                    if (db.isBlacklisted(ip)) {
                        continue;
                    }

                    ServerPinger.PingResult result = pinger.ping(ip);
                    if (result.online) {
                        logger.info("Scanner found server: " + ip + " (" + result.players + " players)");
                        try {
                           db.addServer(ip, 25565, 0, ip, true);

                           DatabaseManager.ServerInfo s = db.getServer(0, ip);
                           if (s != null) {
                               db.updateServerInfo(s.id(), result.motd, result.favicon, result.version);
                               db.addStat(s.id(), result.players, result.maxPlayers, result.version);
                           }
                        } catch (Exception e) {
                        }
                    } else {
                        db.addToBlacklist(ip, 24 * 60 * 60 * 1000L);
                    }
                }

                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
            }
        }
    }

    private boolean isPrivate(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.") || ip.startsWith("172.16.");
    }

    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= Long.parseLong(parts[i]) << (24 - (8 * i));
        }
        return result;
    }

    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    private record IpRange(long start, long end) {}
}
