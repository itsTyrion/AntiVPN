package de.itsTyrion.antiVPN.bungee;

import de.itsTyrion.antiVPN.util.Check;
import de.itsTyrion.antiVPN.util.LoggerWrapper;
import lombok.val;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class AntiVPNBungee extends Plugin {
    private ConfigBungee config;
    private Check check;

    @Override
    public void onEnable() {
        try {
            config = new ConfigBungee(new File(getDataFolder(), "config.json")).load();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Couldn't load/init config. Shutting down");
            return;
        }
        check = new Check(LoggerWrapper.bungee(getLogger()), config);

        val pluginManager = ProxyServer.getInstance().getPluginManager();

        pluginManager.registerListener(this, new Listener() {
            @EventHandler
            void onLogin(PostLoginEvent event) {
                if (!config.preLogin()) {
                    val player = event.getPlayer();
                    val address = ((InetSocketAddress) player.getSocketAddress()).getAddress().getHostAddress();

                    if (!player.hasPermission(config.bypassPermission()) && check.isBadIP(address, player.getName()))
                        player.disconnect(config.disconnectMessage());
                }
            }

            @EventHandler
            void onLogin(PreLoginEvent e) {
                if (config.preLogin()) {
                    val address = ((InetSocketAddress) e.getConnection().getSocketAddress()).getAddress().getHostAddress();
                    if (check.isBadIP(address, e.getConnection().getName())) {
                        e.setCancelReason(config.disconnectMessage());
                        e.setCancelled(true);
                    }
                }
            }

            @EventHandler
            void onPing(ProxyPingEvent event) {
                if (check.isBadIP(((InetSocketAddress) event.getConnection()).getAddress().getHostAddress(), null)) {
                    val disconnectMessage = config.disconnectMessageWhy();
                    event.setResponse(new ServerPing(new ServerPing.Protocol("nope", 420), null, disconnectMessage, null));
                }
            }
        });
    }
}