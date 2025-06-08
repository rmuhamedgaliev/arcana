package io.github.rmuhamedgaliev.arcana.infrastructure.config

/**
 * Application configuration.
 * Loads configuration from environment variables with default values.
 */
data class AppConfig(
    val telegramBotToken: String = "",
    val gamesDirectory: String = "games",
    val databaseUrl: String = "jdbc:h2:./arcana;AUTO_SERVER=TRUE",
    val databaseUsername: String = "sa",
    val databasePassword: String = "",
    val maxConnectionPoolSize: Int = 10,
    val connectionTimeout: Long = 30000,
    val idleTimeout: Long = 600000
) {
    companion object {
        /**
         * Create a configuration from environment variables.
         *
         * @return The configuration
         */
        fun fromEnvironment(): AppConfig {
            return AppConfig(
                telegramBotToken = System.getenv("TELEGRAM_BOT_TOKEN") ?: "",
                gamesDirectory = System.getenv("GAMES_DIRECTORY") ?: "games",
                databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:h2:./arcana;AUTO_SERVER=TRUE",
                databaseUsername = System.getenv("DATABASE_USERNAME") ?: "sa",
                databasePassword = System.getenv("DATABASE_PASSWORD") ?: "",
                maxConnectionPoolSize = System.getenv("MAX_CONNECTION_POOL_SIZE")?.toIntOrNull() ?: 10,
                connectionTimeout = System.getenv("CONNECTION_TIMEOUT")?.toLongOrNull() ?: 30000,
                idleTimeout = System.getenv("IDLE_TIMEOUT")?.toLongOrNull() ?: 600000
            )
        }
    }
}
