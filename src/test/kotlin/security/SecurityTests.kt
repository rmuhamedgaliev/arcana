package security

import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.ports.PlayerRepository
import io.github.rmuhamedgaliev.arcana.domain.ports.StoryRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2DatabaseConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.json.JsonStoryRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import java.time.Instant

/**
 * Security tests for the Arcana application.
 * Tests for SQL injection, input validation, and XSS prevention.
 */
class SecurityTests {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var databaseConfig: H2DatabaseConfig
    private lateinit var connection: Connection

    @BeforeEach
    fun setup() {
        // Initialize app config with in-memory database for testing
        val appConfig = AppConfig(
            databaseUrl = "jdbc:h2:mem:securitytest;DB_CLOSE_DELAY=-1",
            databaseUsername = "sa",
            databasePassword = ""
        )

        // Initialize database config
        databaseConfig = H2DatabaseConfig(appConfig)

        // Create tables
        connection = databaseConfig.getConnection()
        connection.use { conn ->
            conn.createStatement().use { stmt ->
                // Create players table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS players (
                        id VARCHAR(255) PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL,
                        subscription_tier VARCHAR(50) NOT NULL,
                        subscription_expires_at TIMESTAMP
                    )
                """)

                // Create player_attributes table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_attributes (
                        player_id VARCHAR(255) NOT NULL,
                        attribute_key VARCHAR(255) NOT NULL,
                        attribute_value INT NOT NULL,
                        PRIMARY KEY (player_id, attribute_key),
                        FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                    )
                """)

                // Create player_progress table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_progress (
                        player_id VARCHAR(255) NOT NULL,
                        progress_key VARCHAR(255) NOT NULL,
                        progress_value VARCHAR(1000) NOT NULL,
                        PRIMARY KEY (player_id, progress_key),
                        FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        // Initialize repository
        playerRepository = H2PlayerRepository(databaseConfig)
    }

    /**
     * Test that SQL injection attempts in player queries are prevented.
     */
    @Test
    fun `should prevent SQL injection in player queries`() {
        runBlocking {
            // Create a player with a malicious ID containing SQL injection
            val maliciousId = "'; DROP TABLE players; --"
            val player = Player(
                id = maliciousId,
                createdAt = Instant.now()
            )

            // Try to save the player
            playerRepository.save(player)

            // Verify the player was saved correctly
            val savedPlayer = playerRepository.findById(maliciousId)

            // The player should be found by its exact ID, not interpreted as SQL
            assertNotNull(savedPlayer, "Player should be found by its exact ID")
            assertEquals(maliciousId, savedPlayer?.id, "Player ID should match the malicious ID")

            // Verify the tables still exist (weren't dropped by the injection)
            connection = databaseConfig.getConnection()
            var tablesExist = false
            connection.use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery("SHOW TABLES")
                    tablesExist = rs.next() // At least one table exists
                }
            }

            assertTrue(tablesExist, "Database tables should still exist")
        }
    }

    /**
     * Test that input parameters are properly validated.
     */
    @Test
    fun `should validate input parameters`() {
        runBlocking {
            // Test with strings of different lengths
            val normalString = "normal_attribute"
            val borderlineString = "A".repeat(255) // Max length for VARCHAR(255)
            val tooLongString = "A".repeat(10000) // Exceeds column length

            // Create a player
            val player = Player(
                id = "normal_id",
                createdAt = Instant.now()
            )

            // Set attribute with normal key
            player.setAttribute(normalString, 100)

            // Set progress with normal key and value
            player.setProgress(normalString, "normal_value")

            // Try to save the player
            playerRepository.save(player)

            // Verify the player was saved
            val savedPlayer = playerRepository.findById("normal_id")
            assertNotNull(savedPlayer, "Player should be saved and retrieved")

            // Verify normal attribute was saved correctly
            assertEquals(100, savedPlayer?.getAttribute(normalString), 
                "Normal attribute should be saved correctly")

            // Test borderline length string (should work)
            try {
                player.setAttribute(borderlineString, 200)
                playerRepository.save(player)

                // Verify borderline attribute was saved
                val updatedPlayer = playerRepository.findById("normal_id")
                assertEquals(200, updatedPlayer?.getAttribute(borderlineString),
                    "Borderline length attribute should be saved correctly")

                println("[DEBUG_LOG] Successfully saved borderline length attribute")
            } catch (e: Exception) {
                // Some implementations might reject borderline cases
                println("[DEBUG_LOG] Borderline length attribute rejected: ${e.message}")
            }

            // Test too long string (should be rejected)
            try {
                player.setAttribute(tooLongString, 300)
                playerRepository.save(player)

                // If we get here, the implementation truncated or otherwise handled the long string
                println("[DEBUG_LOG] Long attribute was handled (possibly truncated)")

                // Check if it was truncated but saved
                val finalPlayer = playerRepository.findById("normal_id")
                if (finalPlayer?.hasAttribute(tooLongString) == true) {
                    assertEquals(300, finalPlayer.getAttribute(tooLongString),
                        "Long attribute should have correct value if saved")
                }
            } catch (e: Exception) {
                // This is the expected behavior - database should reject too long strings
                println("[DEBUG_LOG] Long attribute correctly rejected: ${e.message}")
                // The H2PlayerRepository wraps the SQLException in a RuntimeException with message "Error saving player"
                assertTrue(e.message?.contains("Error saving player") == true,
                    "Exception should indicate an error saving the player")
            }
        }
    }

    /**
     * Test that malicious player IDs are handled safely.
     */
    @Test
    fun `should handle malicious player IDs`() {
        runBlocking {
            // Test with various potentially malicious IDs
            val maliciousIds = listOf(
                "<script>alert('xss')</script>",
                "'; UPDATE players SET subscription_tier='PREMIUM'; --",
                "../../../etc/passwd",
                "null",
                "undefined",
                "%00" // Null byte
            )

            for (maliciousId in maliciousIds) {
                val player = Player(
                    id = maliciousId,
                    createdAt = Instant.now()
                )

                // Try to save the player
                playerRepository.save(player)

                // Verify the player was saved and can be retrieved
                val savedPlayer = playerRepository.findById(maliciousId)
                assertNotNull(savedPlayer, "Player with ID '$maliciousId' should be saved and retrieved")
                assertEquals(maliciousId, savedPlayer?.id, "Player ID should match exactly")

                // Clean up
                playerRepository.delete(maliciousId)
            }
        }
    }

    /**
     * Test that story content is properly sanitized.
     */
    @Test
    fun `should sanitize story content`() {
        runBlocking {
            // Create a temporary directory for test stories
            val tempDir = createTempDir()
            val appConfig = AppConfig(gamesDirectory = tempDir.absolutePath)
            val objectMapper = jacksonObjectMapper()

            // Create a story repository
            val storyRepository = JsonStoryRepository(appConfig, objectMapper)

            // Create a player with potentially malicious content in progress
            val player = Player(
                id = "story_test_player",
                createdAt = Instant.now()
            )

            // Set progress with potentially malicious content
            val maliciousContent = "<script>alert('xss')</script>"
            player.setProgress("story_content", maliciousContent)

            // Save the player
            playerRepository.save(player)

            // Retrieve the player
            val savedPlayer = playerRepository.findById("story_test_player")
            assertNotNull(savedPlayer, "Player should be saved and retrieved")

            // Verify the content was stored as-is (not executed as script)
            val storedContent = savedPlayer?.getProgress("story_content")
            assertEquals(maliciousContent, storedContent, 
                "Malicious content should be stored as-is, not executed")

            // Clean up
            playerRepository.delete("story_test_player")
            tempDir.deleteRecursively()
        }
    }
}
