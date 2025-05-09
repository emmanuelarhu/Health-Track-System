package main.java.hospital.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton class that manages database connections for the HealthTrack System.
 */
public class DatabaseConnection {
    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);

    // Database connection properties
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/hospital_db";
    private static final String DB_USER = "Emmanuel Arhu";
    private static final String DB_PASSWORD = "admin"; // Replace with your database password

    // Singleton instance
    private static DatabaseConnection instance;

    // Connection object
    private Connection connection;

    /**
     * Private constructor to prevent instantiation from outside.
     */
    private DatabaseConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load JDBC driver", e);
            throw new RuntimeException("Failed to load JDBC driver", e);
        }
    }

    /**
     * Get the singleton instance of DatabaseConnection.
     *
     * @return The DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get a connection to the database.
     *
     * @return A Connection object
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                logger.info("Database connection established");
            } catch (SQLException e) {
                logger.error("Failed to connect to the database", e);
                throw e;
            }
        }
        return connection;
    }

    /**
     * Close the database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Failed to close database connection", e);
            }
        }
    }
}