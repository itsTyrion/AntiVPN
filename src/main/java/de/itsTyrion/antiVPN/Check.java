package de.itsTyrion.antiVPN;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.IOException;
import java.net.URL;

/**
 * This plugin uses the iplegit.com API, which is, without payment, limited to 5000 requests per day.
 *
 * @author itsTyrion
 * Created on 20.01.2021
 */
class Check {
    private static final Component kickMessage = LegacyComponentSerializer.legacySection().deserialize(
            AntiVPN.getConfig().getString("kickMessage", "VPN's are not allowed").replaceAll("&([0-9a-flmnok])", "§$1")
    );

    static void preLogin(PreLoginEvent e) {
        val ip = e.getConnection().getRemoteAddress().getAddress().getHostAddress();
        try {
            if (isIPBad(ip)) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickMessage));

                if (AntiVPN.getConfig().getBoolean("logFailedAttempts", true)) {
                    AntiVPN.getInstance().getLogger().info(e.getUsername() + " tried to join with VPN (" + ip + ")");
                }
            }
        } catch (IOException | JsonParserException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isIPBad(String ip) throws IOException, JsonParserException {
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