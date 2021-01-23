package de.itsTyrion.antiVPN;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * This plugin uses the iplegit.com API, which is, without payment, limited to 5000 requests per day.
 *
 * @author itsTyrion
 * Created on 06.01.2021
 */
enum IPCache {
    INSTANCE;

    private final Cache<String, Boolean> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(AntiVPN.getConfig().getLong("ipCacheDuration", 6L), TimeUnit.HOURS)
            .build();


    void add(String ip, boolean bad) {
        cache.put(ip, bad);
    }

    @Nullable
    Boolean isBadIP(String ip) {
        return cache.getIfPresent(ip);
    }
}