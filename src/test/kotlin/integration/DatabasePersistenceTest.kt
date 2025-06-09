package integration

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.player.PlayerJourney
import io.github.rmuhamedgaliev.arcana.domain.ports.PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2DatabaseConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import java.sql.SQLException
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for database persistence.
 * Uses a real H2 in-memory database to verify persistence operations.
 */
class DatabasePersistenceTest {
    
    private lateinit var appConfig: AppConfig
    private lateinit var databaseConfig: H2DatabaseConfig
    private lateinit var playerRepository: PlayerRepository
    private lateinit var connection: Connection
    
    @BeforeEach
    fun setUp() {
        // Create test-specific configuration with in-memory database
        appConfig = AppConfig(
            databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            databaseUsername = "sa",
            databasePassword = ""
        )
        
        // Initialize database
        databaseConfig = H2DatabaseConfig(appConfig)
        databaseConfig.initialize()
        
        // Create repository
        playerRepository = H2PlayerRepository(databaseConfig)
        
        // Get connection for direct database operations
        connection = databaseConfig.getConnection()
    }
    
    @AfterEach
    fun tearDown() {
        // Close connection
        if (::connection.isInitialized) {
            connection.close()
        }
    }
    
    @Test
    fun `should save and load player with attributes`() = runBlocking {
        // Create a player with attributes
        val player = Player(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now(),
            attributes = mutableMapOf("strength" to 10, "intelligence" to 15),
            progress = mutableMapOf(),
            subscriptionTier = SubscriptionTier.FREE
        )
        
        // Save the player
        playerRepository.save(player)
        
        // Load the player
        val loadedPlayer = playerRepository.findById(player.id)
        
        // Verify the player was loaded correctly
        assertNotNull(loadedPlayer)
        assertEquals(player.id, loadedPlayer.id)
        assertEquals(player.createdAt.toEpochMilli(), loadedPlayer.createdAt.toEpochMilli())
        assertEquals(player.subscriptionTier, loadedPlayer.subscriptionTier)
        
        // Verify attributes
        assertEquals(2, loadedPlayer.attributes.size)
        assertEquals(10, loadedPlayer.getAttribute("strength"))
        assertEquals(15, loadedPlayer.getAttribute("intelligence"))
    }
    
    @Test
    fun `should save and load player progress`() = runBlocking {
        // Create a player with progress
        val player = Player(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now(),
            attributes = mutableMapOf(),
            progress = mutableMapOf("level" to "5", "quest" to "dragon_slayer"),
            subscriptionTier = SubscriptionTier.FREE
        )
        
        // Save the player
        playerRepository.save(player)
        
        // Load the player
        val loadedPlayer = playerRepository.findById(player.id)
        
        // Verify the player was loaded correctly
        assertNotNull(loadedPlayer)
        
        // Verify progress
        assertEquals(2, loadedPlayer.progress.size)
        assertEquals("5", loadedPlayer.getProgress("level"))
        assertEquals("dragon_slayer", loadedPlayer.getProgress("quest"))
    }
    
    @Test
    fun `should update player subscription correctly`() = runBlocking {
        // Create a player with FREE subscription
        val player = Player(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now(),
            subscriptionTier = SubscriptionTier.FREE
        )
        
        // Save the player
        playerRepository.save(player)
        
        // Update subscription to PREMIUM
        val expirationDate = Instant.now().plusSeconds(86400) // 1 day later
        player.subscriptionTier = SubscriptionTier.PREMIUM
        player.subscriptionExpiresAt = expirationDate
        playerRepository.save(player)
        
        // Load the player
        val loadedPlayer = playerRepository.findById(player.id)
        
        // Verify subscription was updated
        assertNotNull(loadedPlayer)
        assertEquals(SubscriptionTier.PREMIUM, loadedPlayer.subscriptionTier)
        assertNotNull(loadedPlayer.subscriptionExpiresAt)
        
        // Verify subscription is active
        assertTrue(loadedPlayer.hasActiveSubscription())
        
        // Update subscription to expired
        val pastDate = Instant.now().minusSeconds(86400) // 1 day ago
        player.subscriptionExpiresAt = pastDate
        playerRepository.save(player)
        
        // Load the player again
        val expiredPlayer = playerRepository.findById(player.id)
        
        // Verify subscription is not active
        assertNotNull(expiredPlayer)
        assertEquals(SubscriptionTier.PREMIUM, expiredPlayer.subscriptionTier)
        assertNotNull(expiredPlayer.subscriptionExpiresAt)
        assertTrue(!expiredPlayer.hasActiveSubscription())
    }
    
    @Test
    fun `should delete player and cascade data`() = runBlocking {
        // Create a player with attributes and progress
        val player = Player(
            id = UUID.randomUUID().toString(),
            attributes = mutableMapOf("strength" to 10),
            progress = mutableMapOf("level" to "5")
        )
        
        // Save the player
        playerRepository.save(player)
        
        // Verify player exists
        val savedPlayer = playerRepository.findById(player.id)
        assertNotNull(savedPlayer)
        
        // Delete the player
        playerRepository.delete(player.id)
        
        // Verify player was deleted
        val deletedPlayer = playerRepository.findById(player.id)
        assertNull(deletedPlayer)
        
        // Verify attributes were cascaded (using direct SQL)
        val stmt = connection.createStatement()
        val attributesRs = stmt.executeQuery(
            "SELECT COUNT(*) FROM player_attributes WHERE player_id = '${player.id}'"
        )
        attributesRs.next()
        assertEquals(0, attributesRs.getInt(1))
        
        // Verify progress was cascaded
        val progressRs = stmt.executeQuery(
            "SELECT COUNT(*) FROM player_progress WHERE player_id = '${player.id}'"
        )
        progressRs.next()
        assertEquals(0, progressRs.getInt(1))
    }
    
    @Test
    fun `should handle concurrent player updates`() = runBlocking {
        // Create a player
        val player = Player(
            id = UUID.randomUUID().toString(),
            attributes = mutableMapOf("strength" to 10)
        )
        
        // Save the player
        playerRepository.save(player)
        
        // Perform concurrent updates
        val updateCount = 10
        val updateJobs = (1..updateCount).map { i ->
            async(Dispatchers.IO) {
                val loadedPlayer = playerRepository.findById(player.id)
                loadedPlayer?.setAttribute("strength", 10 + i)
                if (loadedPlayer != null) {
                    playerRepository.save(loadedPlayer)
                }
            }
        }
        
        // Wait for all updates to complete
        awaitAll(*updateJobs.toTypedArray())
        
        // Load the final player state
        val finalPlayer = playerRepository.findById(player.id)
        
        // Verify the player was updated
        assertNotNull(finalPlayer)
        assertTrue(finalPlayer.getAttribute("strength") > 10)
        
        // The exact value depends on the order of execution, but it should be one of the values we set
        val possibleValues = (11..10 + updateCount).toSet()
        assertTrue(finalPlayer.getAttribute("strength") in possibleValues)
    }
}
