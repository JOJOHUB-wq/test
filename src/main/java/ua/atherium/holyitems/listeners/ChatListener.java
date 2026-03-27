package ua.atherium.holyitems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.holyitems.HolyWorldItems;
import ua.atherium.holyitems.guis.ItemCreatorGUI;
import ua.atherium.holyitems.utils.ChatUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatListener implements Listener {

    private final HolyWorldItems plugin;
    private final Map<UUID, Consumer<String>> pendingInputs = new ConcurrentHashMap<>();

    public ChatListener(HolyWorldItems plugin) {
        this.plugin = plugin;
    }

    public void requestInput(Player player, Consumer<String> callback) {
        pendingInputs.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(ChatUtil.color("&eВведите значение в чат (или 'cancel' для отмены):"));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (pendingInputs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();
            Consumer<String> callback = pendingInputs.remove(player.getUniqueId());

            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatUtil.color("&cОтменено."));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new ItemCreatorGUI(plugin, player).open();
                    }
                }.runTask(plugin);
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    callback.accept(message);
                    new ItemCreatorGUI(plugin, player).open();
                }
            }.runTask(plugin);
        }
    }
}
