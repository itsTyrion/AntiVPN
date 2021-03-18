package de.itsTyrion.antiVPN;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
class Check {
    private final AntiVPN antiVPN;
    private final JsonObject config;
    private final IPCache ipCache = new IPCache();

    private final Component kickMessage = LegacyComponentSerializer.legacySection().deserialize(
            config.getString("kickMessage", "VPN's are not allowed").replaceAll("&([0-9a-flmnok])", "§$1")
    );

    void preLogin(PreLoginEvent event) {
        if (isBadIP(event.getConnection().getRemoteAddress().getAddress().getHostAddress(), event.getUsername())) {

            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickMessage));
        }
    }

    void onLogin(LoginEvent event) {
        val player = event.getPlayer();

        if (!player.hasPermission(config.getString("bypassPermission"))) {
            if (isBadIP(player.getRemoteAddress().getAddress().getHostAddress(), player.getUsername())) {

                event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
            }
        }
    }

    boolean isBadIP(String address, String username) {
        try {
            if (queryBadIp(address)) {
                if (config.getBoolean("logFailedAttempts", true) && username != null) {
                    antiVPN.getLogger().info("Blocked " + username + " from joining (" + address + ")");
                }
                return true;
            }
        } catch (MalformedURLException | JsonParserException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean queryBadIp(String ip) throws MalformedURLException, JsonParserException {
        Boolean bad = ipCache.isBadIP(ip);
        if (bad != null)
            return bad;

        bad = JsonParser.object().from(new URL("https://api.iplegit.com/info?ip=" + ip)).getBoolean("bad");
        ipCache.add(ip, bad);

        return bad;
    }

    /**
     * This plugin uses the iplegit.com API, which is, without payment, limited to 5000 requests per day.
     *
     * @author itsTyrion
     * Created on 06.01.2021
     */
    class IPCache {
        private final Cache<String, Boolean> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getLong("ipCacheDuration", 6L), TimeUnit.HOURS)
                .build();


        void add(String ip, boolean bad) {
            cache.put(ip, bad);
        }

        @Nullable
        Boolean isBadIP(String ip) {
            return cache.getIfPresent(ip);
        }
    }
}