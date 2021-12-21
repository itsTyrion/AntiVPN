package de.itsTyrion.antiVPN.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import lombok.AllArgsConstructor;
import lombok.val;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to check incoming log-in requests.
 *
 * @author itsTyrion
 * Created on 20.01.2021
 */
@AllArgsConstructor
public class Check {
    private final LoggerWrapper loggerWrapper;
    private final Config config;
    private final IPCache ipCache = new IPCache();

    public boolean isBadIP(String address, String username) {
        try {
            if (queryBadIp(address)) {
                if (config.logFailedAttempts() && username != null)
                    loggerWrapper.info("Blocked " + username + " from joining (" + address + ")");
                return true;
            }
        } catch (IOException | JsonParserException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean queryBadIp(String ip) throws IOException, JsonParserException {
        Boolean bad = ipCache.isBadIP(ip);
        if (bad != null)
            return bad;

        val url = new URL("http://v2.api.iphub.info/ip/" + ip);
        val con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("X-Key", config.apiKey());

        if (con.getResponseCode() == 429) {
            loggerWrapper.error("IPHub API Limit reached! Cannot continue!");
            con.disconnect();
            return config.allowConnectionWhenExceeded();
        }

        bad = JsonParser.object().from(con.getInputStream()).getInt("block") == 1;
        ipCache.add(ip, bad);

        return bad;
    }

    /**
     * This plugin uses the iphub.info API, which is, without payment, limited to 1000 requests per day.
     *
     * @author itsTyrion
     * Created on 06.01.2021
     */
    class IPCache {
        private final Cache<String, Boolean> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(config.ipCacheDuration(), TimeUnit.HOURS)
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