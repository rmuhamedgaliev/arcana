package io.github.rmuhamedgaliev.arcana.infrastructure.database

import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

/**
 * Configuration for H2 database.
 *
 * H2 was chosen for the following reasons:
 * 1. Embedded mode - can be packaged with the application
 * 2. SQL compatibility - supports standard SQL syntax
 * 3. Performance - good performance for small to medium datasets
 * 4. Durability - supports transaction logging and recovery
 * 5. Flexibility - can be used in embedded or server mode
 */
class H2DatabaseConfig(private val appConfig: AppConfig) {
    private var initialized = false

    /**
     * Initialize the database.
     */
    fun initialize() {
        try {
            // Load the H2 JDBC driver
            Class.forName("org.h2.Driver")

            // Test the connection
            testConnection()

            // Initialize the database schema
            initializeSchema()

            initialized = true
            System.out.println("Database initialized successfully")
        } catch (e: Exception) {
            System.err.println("Failed to initialize database: " + e.message)
            throw RuntimeException("Failed to initialize database", e)
        }
    }

    /**
     * Get a connection to the database.
     *
     * @return A database connection
     */
    fun getConnection(): Connection {
        return DriverManager.getConnection(
            appConfig.databaseUrl,
            appConfig.databaseUsername,
            appConfig.databasePassword
        )
    }

    /**
     * Test the database connection.
     */
    private fun testConnection() {
        var connection: Connection? = null
        var statement: Statement? = null

        try {
            connection = getConnection()
            statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT 1")

            if (resultSet.next()) {
                System.out.println("Database connection test successful")
            } else {
                throw SQLException("Database connection test failed")
            }
        } catch (e: SQLException) {
            System.err.println("Database connection test failed: " + e.message)
            throw RuntimeException("Database connection test failed", e)
        } finally {
            if (statement != null) {
                try {
                    statement.close()
                } catch (e: SQLException) {
                    // Ignore
                }
            }

            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Initialize the database schema.
     */
    private fun initializeSchema() {
        var connection: Connection? = null
        var statement: Statement? = null

        try {
            connection = getConnection()
            statement = connection.createStatement()

            // Create players table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS players (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "subscription_tier VARCHAR(50) NOT NULL DEFAULT 'FREE', " +
                "subscription_expires_at TIMESTAMP" +
                ")"
            )

            // Create player_attributes table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS player_attributes (" +
                "player_id VARCHAR(255) NOT NULL, " +
                "attribute_key VARCHAR(255) NOT NULL, " +
                "attribute_value INT NOT NULL, " +
                "PRIMARY KEY (player_id, attribute_key), " +
                "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE" +
                ")"
            )

            // Create player_progress table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS player_progress (" +
                "player_id VARCHAR(255) NOT NULL, " +
                "progress_key VARCHAR(255) NOT NULL, " +
                "progress_value VARCHAR(4000) NOT NULL, " +
                "PRIMARY KEY (player_id, progress_key), " +
                "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE" +
                ")"
            )

            // Create player_journeys table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS player_journeys (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "player_id VARCHAR(255) NOT NULL, " +
                "story_id VARCHAR(255) NOT NULL, " +
                "current_beat_id VARCHAR(255) NOT NULL, " +
                "started_at TIMESTAMP NOT NULL, " +
                "last_updated_at TIMESTAMP NOT NULL, " +
                "completed_at TIMESTAMP, " +
                "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE" +
                ")"
            )

            // Create journey_choices table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS journey_choices (" +
                "journey_id VARCHAR(255) NOT NULL, " +
                "choice_id VARCHAR(255) NOT NULL, " +
                "beat_id VARCHAR(255) NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL, " +
                "PRIMARY KEY (journey_id, choice_id, beat_id), " +
                "FOREIGN KEY (journey_id) REFERENCES player_journeys(id) ON DELETE CASCADE" +
                ")"
            )

            // Create journey_arcs table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS journey_arcs (" +
                "journey_id VARCHAR(255) NOT NULL, " +
                "arc_id VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (journey_id, arc_id), " +
                "FOREIGN KEY (journey_id) REFERENCES player_journeys(id) ON DELETE CASCADE" +
                ")"
            )

            // Create journey_attributes table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS journey_attributes (" +
                "journey_id VARCHAR(255) NOT NULL, " +
                "attribute_key VARCHAR(255) NOT NULL, " +
                "attribute_value INT NOT NULL, " +
                "PRIMARY KEY (journey_id, attribute_key), " +
                "FOREIGN KEY (journey_id) REFERENCES player_journeys(id) ON DELETE CASCADE" +
                ")"
            )

            // Create payments table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "player_id VARCHAR(255) NOT NULL, " +
                "amount DOUBLE NOT NULL, " +
                "currency VARCHAR(10) NOT NULL, " +
                "payment_method VARCHAR(50) NOT NULL, " +
                "item_type VARCHAR(50) NOT NULL, " +
                "item_id VARCHAR(255) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "updated_at TIMESTAMP NOT NULL, " +
                "transaction_id VARCHAR(255), " +
                "provider_reference VARCHAR(255), " +
                "error_code VARCHAR(50), " +
                "error_message VARCHAR(1000), " +
                "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE" +
                ")"
            )

            // Create subscriptions table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS subscriptions (" +
                "id VARCHAR(255) PRIMARY KEY, " +
                "player_id VARCHAR(255) NOT NULL, " +
                "tier VARCHAR(50) NOT NULL, " +
                "start_date TIMESTAMP NOT NULL, " +
                "end_date TIMESTAMP NOT NULL, " +
                "auto_renew BOOLEAN NOT NULL DEFAULT FALSE, " +
                "status VARCHAR(50) NOT NULL, " +
                "payment_id VARCHAR(255), " +
                "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL" +
                ")"
            )

            System.out.println("Database schema initialized successfully")
        } catch (e: SQLException) {
            System.err.println("Failed to initialize database schema: " + e.message)
            throw RuntimeException("Failed to initialize database schema", e)
        } finally {
            if (statement != null) {
                try {
                    statement.close()
                } catch (e: SQLException) {
                    // Ignore
                }
            }

            if (connection != null) {
                try {
                    connection.close()
                } catch (e: SQLException) {
                    // Ignore
                }
            }
        }
    }
}
