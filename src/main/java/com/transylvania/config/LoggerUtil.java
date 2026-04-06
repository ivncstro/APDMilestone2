package com.transylvania.config;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("HotelSystemLogger");

    static {
        try {
            FileHandler fileHandler = new FileHandler("system_logs.%g.log", 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LoggerUtil() {
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void logInfo(String actor, String action, String entityType, String entityId, String message) {
        logger.info(formatLog(actor, action, entityType, entityId, message));
    }

    public static void logWarning(String actor, String action, String entityType, String entityId, String message) {
        logger.warning(formatLog(actor, action, entityType, entityId, message));
    }

    public static void logSevere(String actor, String action, String entityType, String entityId, String message, Exception e) {
        logger.log(Level.SEVERE, formatLog(actor, action, entityType, entityId, message), e);
    }

    private static String formatLog(String actor, String action, String entityType, String entityId, String message) {
        return "actor=" + actor
                + ", action=" + action
                + ", entityType=" + entityType
                + ", entityId=" + entityId
                + ", message=" + message;
    }
}