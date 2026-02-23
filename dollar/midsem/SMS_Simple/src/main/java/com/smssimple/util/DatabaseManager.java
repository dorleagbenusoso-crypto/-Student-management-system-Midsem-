package com.smssimple.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema initialisation.
 * Provides a single shared connection for the application lifetime.
 */
public class DatabaseManager {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE = DB_FOLDER + File.separator + "students.db";
    private static Connection connection;

    private DatabaseManager() {}

    /**
     * Returns the shared database connection, creating it if necessary.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            File folder = new File(DB_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            initialiseSchema(connection);
        }
        return connection;
    }

    /**
     * Creates the students table if it does not already exist.
     * Enforces NOT NULL and CHECK constraints as required.
     */
    private static void initialiseSchema(Connection conn) throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS students (
                student_id   TEXT PRIMARY KEY,
                full_name    TEXT NOT NULL,
                programme    TEXT NOT NULL,
                level        INTEGER NOT NULL
                             CHECK(level IN (100,200,300,400,500,600,700)),
                gpa          REAL NOT NULL
                             CHECK(gpa >= 0.0 AND gpa <= 4.0),
                email        TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                date_added   TEXT NOT NULL,
                status       TEXT NOT NULL DEFAULT 'ACTIVE'
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute(createTable);
        }
    }

    /**
     * Closes the database connection on application shutdown.
     */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                AppLogger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            AppLogger.error("Error closing database connection: " + e.getMessage());
        }
    }
}
