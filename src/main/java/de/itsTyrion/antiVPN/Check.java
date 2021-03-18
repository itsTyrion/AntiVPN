package de.itsTyrion.antiVPN;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to check incoming log-in requests.
 *
 * @author itsTyrion
 * Created on 20.01.2021
 */
class Check {
    private static final Component kickMessage = LegacyComponentSerializer.legacySection().deserialize(
            AntiVPN.getConfig().getString("kickMessage", "VPN's are not allowed").replaceAll("&([0-9a-flmnok])", "§$1")
    );

    static void preLogin(PreLoginEvent event) {
        if (isBadIP(event.getConnection().getRemoteAddress().getAddress().getHostAddress(), event.getUsername())) {

            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickMessage));
        }
    }

    static void onLogin(LoginEvent event) {
        val player = event.getPlayer();

        if (!player.hasPermission(AntiVPN.getConfig().getString("bypassPermission"))) {
            if (isBadIP(player.getRemoteAddress().getAddress().getHostAddress(), player.getUsername())) {

                event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
            }
        }
    }

    static boolean isBadIP(String address, String username) {
        try {
            if (queryBadIp(address)) {
                if (AntiVPN.getConfig().getBoolean("logFailedAttempts", true)) {
                    AntiVPN.getInstance().getLogger().info("Blocked " + username + " from joining (" + address + ")");
                }
                return true;
            }
        } catch (MalformedURLException | JsonParserException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean queryBadIp(String ip) throws MalformedURLException, JsonParserException {
        Boolean bad = IPCache.isBadIP(ip);
        if (bad != null)
            return bad;

        bad = JsonParser.object().from(new URL("https://api.iplegit.com/info?ip=" + ip)).getBoolean("bad");
        IPCache.add(ip, bad);

        return bad;
    }

    /**
     * This plugin uses the iplegit.com API, which is, without payment, limited to 5000 requests per day.
     *
     * @author itsTyrion
     * Created on 06.01.2021
     */
    static class IPCache {
        private static final Cache<String, Boolean> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(AntiVPN.getConfig().getLong("ipCacheDuration", 6L), TimeUnit.HOURS)
                .build();


        static void add(String ip, boolean bad) {
            cache.put(ip, bad);
        }

        @Nullable
        static Boolean isBadIP(String ip) {
            return cache.getIfPresent(ip);
        }
    }
}