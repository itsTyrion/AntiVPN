package de.itsTyrion.antiVPN.util;

import lombok.AllArgsConstructor;
import lombok.val;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@AllArgsConstructor
public abstract class Config {

    protected final File configFile;
    protected static final int CONFIG_VERSION = 6;


    /**
     * Checks whether the config file is present and in the current version on-disk.
     * If not, it's copied from the .jar file. If there was an outdated config, it's backed up
     *
     * @throws IOException If the file could not be read or written
     * @return this instance
     */
    protected abstract Config init() throws IOException;

    /**
     * Tries to read the plugin's configuration file and parse it.
     *
     * @throws IOException If the file could not be read or written
     */
    protected abstract void load() throws IOException;

    protected final void createConfigFile(boolean oldVersionPresent) throws IOException {
        if (oldVersionPresent) {
            Files.move(configFile.toPath(), configFile.toPath().getParent().resolve("config_old.json"));
            System.err.println("New config version! Your old config has been renamed to config_old");
        } else
            System.err.println("No config file found! Please set up.");
        if (!configFile.exists()) {
            try (val input = getClass().getResourceAsStream("/" + configFile.getName())) {
                if (input == null) {
                    throw new IOException("Could not read config from jar. Please re-download.");
                } else
                    Files.copy(input, configFile.toPath());
            }
        }
    }


    /**
     * Returns the configured message to disconnect players with.
     * Since BungeeCord have different/incompatible implementations
     * of the Minecraft ChatComponent system, the type is specified as Object
     */
    @NonNull
    public abstract Object disconnectMessage();

    /**
     * if {getPreLogin} is set to true, players with the returned permission can bypass the detection
     */
    @NonNull
    public abstract String bypassPermission();

    /**
     * Whether to check incoming connections before fully processing them, or not.
     * While this saves resources and fixes the DoS option of rate-limiting the proxy,
     * it doesn't allow configuring a bypass permission because no player UUID is known yet.
     */
    public abstract boolean preLogin();

    /**
     * When enables, every blocked connection (Username + IP) will be logged to console.
     */
    public abstract boolean logFailedAttempts();

    /**
     * How many hours should the plugin cache whether an IP address is considered "good" or "bad"?
     * (Less caching may lead to reaching the daily API request limit of the IP database faster)
     */
    public abstract int ipCacheDuration();

    /**
     * The plug-in uses iphub.info, which, at the time of writing, requires an API key
     */
    public abstract String apiKey();

    /**
     * Once the API limit has been reached, should players be allowed to connect?
     */
    public abstract boolean allowConnectionWhenExceeded();
}