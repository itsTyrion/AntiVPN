package de.itsTyrion.antiVPN;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.val;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "antivpn", name = "AntiVPN", version = "1.1", authors = {"itsTyrion"})
public class AntiVPN {
    private final ProxyServer server;

    @Getter
    private static AntiVPN instance;
    @Getter
    private static Toml config;
    @Getter
    private final Logger logger;

    @Inject
    public AntiVPN(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        this.server = server;
        this.logger = logger;
        instance = this;
        try {
            config = loadConfig(folder); // try to load the configuration file
        } catch (IOException e) {
            e.printStackTrace();
            config = new Toml();
        }
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        server.getEventManager().register(this, PreLoginEvent.class, Check::preLogin);
    }

    /**
     * Tries to read the plugin's configuration file and parse it.
     * In case it doesn't exist, the default configuration contained in the .jar will be written.
     *
     * @throws IOException If the file could not be read or if the default config could not be written
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // We don't need to know if a file was created
    private Toml loadConfig(Path path) throws IOException {
        val folder = path.toFile();
        val file = new File(folder, "config.toml");

        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (!file.exists()) {
            try (val input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            }
        }

        return new Toml().read(file);
    }
}