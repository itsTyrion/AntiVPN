package de.itsTyrion.antiVPN;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.MalformedURLException;
import java.net.URL;

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

    static void preLogin(PreLoginEvent e) {
        if (isBadIP(e.getConnection().getRemoteAddress().getAddress().getHostAddress(), e.getUsername())) {

            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickMessage));
        }
    }

    static void onLogin(LoginEvent e) {
        if (e.getPlayer().hasPermission(AntiVPN.getConfig().getString("bypassPermission"))) {
            return;
        }
        if (isBadIP(e.getPlayer().getRemoteAddress().getAddress().getHostAddress(), e.getPlayer().getUsername())) {

            e.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
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
        val bad = IPCache.INSTANCE.isBadIP(ip);
        if (bad != null)
            return bad;

        val json = JsonParser.object().from(new URL("https://api.iplegit.com/info?ip=" + ip));

        if (json.getBoolean("bad")) {
            IPCache.INSTANCE.add(ip, true);
            return true;
        }

        IPCache.INSTANCE.add(ip, false);
        return false;
    }
}