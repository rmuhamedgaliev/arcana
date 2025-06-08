package io.github.rmuhamedgaliev.arcana.performance

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.json.JsonStoryRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2DatabaseConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.application.events.SimpleEventBus
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

class PerformanceTests {

    @Test
    fun `DEMO - measure startup time`() {
        val startTime = System.currentTimeMillis()

        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val dbConfig = H2DatabaseConfig(appConfig)
        val eventBus = SimpleEventBus()
        val objectMapper = jacksonObjectMapper()
        val gameService = GameApplicationService(appConfig, eventBus)

        val endTime = System.currentTimeMillis()
        val startupTime = endTime - startTime

        println("Startup time: ${startupTime}ms")
        assertTrue(startupTime < 10000, "Startup time should be less than 10 seconds, but was ${startupTime}ms")
    }

    @Test
    fun `DEMO - measure choice processing latency`() = runBlocking {
        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val eventBus = SimpleEventBus()
        val gameService = GameApplicationService(appConfig, eventBus)

        // Create a test player
        val playerId = UUID.randomUUID().toString()
        val playerName = "TestPlayer"
        val player = gameService.getOrCreatePlayer(playerId, playerName)

        // Load a game
        val storyId = "adventure" // Assuming this game exists
        val startBeat = gameService.startStory(player.id, storyId)

        // Measure choice processing time
        val startTime = System.currentTimeMillis()

        // Process a choice (assuming the first choice is valid)
        if (startBeat != null && startBeat.choices.isNotEmpty()) {
            val choiceId = startBeat.choices.first().id
            gameService.makeChoice(player.id, storyId, choiceId)
        }

        val endTime = System.currentTimeMillis()
        val processingTime = endTime - startTime

        println("Choice processing latency: ${processingTime}ms")
        assertTrue(processingTime < 500, "Choice processing should be less than 500ms, but was ${processingTime}ms")
    }

    @Test
    fun `DEMO - concurrent players stress test`() = runBlocking {
        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val eventBus = SimpleEventBus()
        val gameService = GameApplicationService(appConfig, eventBus)

        val numberOfPlayers = 50
        val results = ConcurrentHashMap<String, Boolean>()

        val startTime = System.currentTimeMillis()

        // Create and run multiple virtual players
        val players = (1..numberOfPlayers).map { playerNum ->
            async(Dispatchers.IO) {
                try {
                    val playerId = "virtual-player-$playerNum"
                    val playerName = "VirtualPlayer$playerNum"
                    val player = gameService.getOrCreatePlayer(playerId, playerName)

                    // Start story
                    val storyId = "adventure"
                    val startBeat = gameService.startStory(player.id, storyId)

                    // Make some choices
                    if (startBeat != null && startBeat.choices.isNotEmpty()) {
                        val choiceId = startBeat.choices.first().id
                        val nextBeat = gameService.makeChoice(player.id, storyId, choiceId)

                        if (nextBeat != null && nextBeat.choices.isNotEmpty()) {
                            val nextChoiceId = nextBeat.choices.first().id
                            gameService.makeChoice(player.id, storyId, nextChoiceId)
                        }
                    }

                    results[playerId] = true
                    true
                } catch (e: Exception) {
                    println("[DEBUG_LOG] Player failed: ${e.message}")
                    false
                }
            }
        }

        // Wait for all players to complete
        players.awaitAll()

        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        val successCount = results.values.count { it }

        println("Concurrent players test completed in ${totalTime}ms")
        println("Successfully processed $successCount out of $numberOfPlayers players")

        assertTrue(successCount >= numberOfPlayers * 0.9, 
            "At least 90% of players should complete successfully, but only $successCount out of $numberOfPlayers did")
    }
}
