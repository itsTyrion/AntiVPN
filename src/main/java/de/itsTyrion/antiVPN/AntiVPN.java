package de.itsTyrion.antiVPN;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "antivpn", name = "AntiVPN", version = "@VERSION@", authors = {"itsTyrion"})
public class AntiVPN {
    private final ProxyServer server;
    private final JsonObject config;
    private final Check check;

    @Getter
    private final Logger logger;

    @Inject
    public AntiVPN(ProxyServer server, Logger logger, @DataDirectory Path folder) throws JsonParserException {
        this.server = server;
        this.logger = logger;

        JsonObject json = new JsonObject();
        try {
            json = loadConfig(folder); // try to load the configuration file
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = json;
        check = new Check(this, config);
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        if (config.getBoolean("preLogin", true)) {
            server.getEventManager().register(this, PreLoginEvent.class, check::preLogin);
        } else
            server.getEventManager().register(this, LoginEvent.class, check::onLogin);
    }

    /**
     * Tries to read the plugin's configuration file and parse it.
     * In case it doesn't exist, the default configuration contained in the .jar will be written.
     *
     * @throws IOException If the file could not be read or if the default config could not be written
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // We don't need to know if a file was created
    private JsonObject loadConfig(Path path) throws IOException, JsonParserException {
        val folder = path.toFile();
        val file = new File(folder, "config.json");

        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (!file.exists()) {
            try (val input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input == null) {
                    throw new IOException("Could not read config from jar. Please re-download.");
                } else {
                    Files.copy(input, file.toPath());
                }
            }
        }

        return JsonParser.object().from(new FileInputStream(file));
    }
}