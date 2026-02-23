package com.smssimple.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file-based logger.
 * Writes logs to data/app.log.
 * IMPORTANT: Never logs full personal student records.
 */
public class AppLogger {

    private static final String LOG_FOLDER = "data";
    private static final String LOG_FILE = LOG_FOLDER + File.separator + "app.log";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public static void info(String message) {
        log("INFO ", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void warn(String message) {
        log("WARN ", message);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logLine = "[" + timestamp + "] [" + level + "] " + message;

        // Print to console for development
        System.out.println(logLine);

        // Write to file
        try {
            File folder = new File(LOG_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                writer.println(logLine);
            }
        } catch (IOException e) {
            System.err.println("Logger failed to write to file: " + e.getMessage());
        }
    }
}
