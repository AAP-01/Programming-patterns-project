package vanier.conference_management_system.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());

    private static final String DEFAULT_URL = "jdbc:sqlite:conference.db";

    private static String url = DEFAULT_URL;

    private Database() {

    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    public static void resetUrl() {
        url = DEFAULT_URL;
    }

    public static void createTables() {
        runScript("database/schema.sql");
    }

    public static void insertSeedData() {
        runScript("database/seed.sql");
    }

    private static void runScript(String resourcePath) {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream("/vanier/conference_management_system/" + resourcePath)) {
            if (in == null) {
                throw new DataAccessException("SQL file not found: " + resourcePath);
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read " + resourcePath, e);
            throw new DataAccessException("Could not read " + resourcePath, e);
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String command : removeComments(sql).split(";")) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not run " + resourcePath, e);
            throw new DataAccessException("Could not run " + resourcePath, e);
        }
    }

    private static String removeComments(String sql) {
        StringBuilder result = new StringBuilder();
        for (String line : sql.split("\n")) {
            int commentStart = line.indexOf("--");
            result.append(commentStart >= 0 ? line.substring(0, commentStart) : line).append("\n");
        }
        return result.toString();
    }
}