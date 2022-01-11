package de.itsTyrion.antiVPN.velocity;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.itsTyrion.antiVPN.util.Check;
import de.itsTyrion.antiVPN.util.LoggerWrapper;
import lombok.val;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "antivpn", name = "AntiVPN", version = "@VERSION@", authors = {"itsTyrion"})
public class AntiVPNVelocity {
    private final ProxyServer server;
    private final ConfigVelocity config;
    private final Check check;

    @Inject
    public AntiVPNVelocity(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        this.server = server;
        ConfigVelocity cfg = null;
        try {
            cfg = new ConfigVelocity(new File(folder.toFile(), "config.json")).init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        check = new Check(LoggerWrapper.velocity(logger), cfg);
        config = cfg;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        if (config == null)
            return;

        if (config.preLogin()) {
            server.getEventManager().register(this, PreLoginEvent.class, e -> {
                if (check.isBadIP(e.getConnection().getRemoteAddress().getAddress().getHostAddress(), e.getUsername()))
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(config.disconnectMessage()));
            });
        } else
            server.getEventManager().register(this, LoginEvent.class, e -> {
                val player = e.getPlayer();

                if (!player.hasPermission(config.bypassPermission())) {
                    if (check.isBadIP(player.getRemoteAddress().getAddress().getHostAddress(), player.getUsername())) {
                        e.setResult(ResultedEvent.ComponentResult.denied(config.disconnectMessage()));
                    }
                }
            });
        server.getEventManager().register(this, ProxyPingEvent.class, e -> {
            if (check.isBadIP(e.getConnection().getRemoteAddress().getAddress().getHostAddress(), null)) {
                val disconnectMessage = config.disconnectMessage();
                e.setPing(new ServerPing(new ServerPing.Version(420, "Nope"), null, disconnectMessage, null));
            }
        });
    }
}