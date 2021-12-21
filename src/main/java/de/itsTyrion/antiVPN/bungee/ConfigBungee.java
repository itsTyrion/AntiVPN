package de.itsTyrion.antiVPN.bungee;

import de.itsTyrion.antiVPN.util.Config;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigBungee extends Config {

    private final ConfigurationProvider configProvider = ConfigurationProvider.getProvider(JsonConfiguration.class);
    private Configuration config;

    public ConfigBungee(File configFile) {
        super(configFile);
    }

    @Override
    protected ConfigBungee load() throws IOException {
        val wrongVersion = config.getInt("VERSION-DontTouch") < 5;
        createConfigFile(wrongVersion);
        config = configProvider.load(configFile);
        return this;
    }

    private final Pattern pattern = Pattern.compile("&([0-9a-flmnokr])"); // Pattern to translate & color codes to §

    @Override
    public BaseComponent @NotNull [] disconnectMessage() {
        return TextComponent.fromLegacyText(pattern.matcher(config.getString("disconnectMessage")).replaceAll("§$1"));
    }

    public @NotNull TextComponent disconnectMessageWhy() {
        return new TextComponent(pattern.matcher(config.getString("disconnectMessage")).replaceAll("§$1"));
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
