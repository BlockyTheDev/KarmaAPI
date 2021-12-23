package ml.karmaconfigs.api.bukkit.listener;

import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.common.utils.UUIDUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class JoinHandler implements Listener {

    private final KarmaPlugin plugin;

    public JoinHandler(final KarmaPlugin owner) {
        plugin = owner;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        String name = e.getName();

        plugin.async().queue(() -> UUIDUtil.registerMinecraftClient(name));
    }
}