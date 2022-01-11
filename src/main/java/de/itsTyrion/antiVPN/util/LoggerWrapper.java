package de.itsTyrion.antiVPN.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggerWrapper {
    private java.util.logging.Logger bungeeLogger;
    private org.slf4j.Logger velocityLogger;

    @Contract("_ -> new")
    public static LoggerWrapper bungee(java.util.logging.Logger logger) {
        return new LoggerWrapper(logger, null);
    }

    @Contract("_ -> new")
    public static LoggerWrapper velocity(org.slf4j.Logger logger) {
        return new LoggerWrapper(null, logger);
    }

    public void info(String msg) {
        if (bungeeLogger != null) {
            bungeeLogger.info(msg);
        } else
            velocityLogger.info(msg);
    }

    public void error(String msg) {
        if (bungeeLogger != null) {
            bungeeLogger.severe(msg);
        } else
            velocityLogger.error(msg);
    }
}
