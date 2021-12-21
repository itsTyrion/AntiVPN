package de.itsTyrion.antiVPN.velocity;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import de.itsTyrion.antiVPN.util.Config;
import lombok.val;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigVelocity extends Config {

    private JsonObject config;

    public ConfigVelocity(File configFile) {
        super(configFile);
    }

    @Override
    protected ConfigVelocity load() throws IOException {
        val wrongVersion = config.getInt("VERSION-DontTouch") < 5;
        createConfigFile(wrongVersion);

        try {
            config = JsonParser.object().from(new FileInputStream(configFile));
        } catch (JsonParserException ex) {
            System.err.println("Couldn't parse config (" + ex.getMessage() + ')');
        }
        return this;
    }

    private final Pattern pattern = Pattern.compile("&([0-9a-flmnokr])"); // Pattern to translate & color codes to §

    @Override
    public @NotNull TextComponent disconnectMessage() {
        val msg = pattern.matcher(config.getString("disconnectMessage")).replaceAll("§$1");
        return LegacyComponentSerializer.legacySection().deserialize(msg);
    }

    @Override
    public boolean preLogin() {
        return config.getBoolean("preLogin", true);
    }

    @Override
    public @NotNull String bypassPermission() {
        return config.getString("bypassPermission", "antivpn.bypass");
    }

    @Override
    public boolean logFailedAttempts() {
        return config.getBoolean("logFailedAttempts");
    }

    @Override
    public int ipCacheDuration() {
        return config.getInt("ipCacheDuration", 6);
    }

    @Override
    public String apiKey() {
        return config.getString("ipHub-Key");
    }

    @Override
    public boolean allowConnectionWhenExceeded() {
        return config.getBoolean("allowConnectionWhenExceeded", true);
    }
}