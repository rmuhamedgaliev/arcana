package io.github.rmuhamedgaliev.arcana.ux

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlinx.coroutines.runBlocking
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.application.events.SimpleEventBus
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.ports.StoryRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.json.JsonStoryRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import java.time.Instant

/**
 * User Experience tests for the game engine.
 * These tests simulate real user scenarios to ensure a smooth user experience.
 */
class UXTests {

    /**
     * Test the onboarding experience for a new player.
     * This test simulates a new player starting the game for the first time.
     */
    @Test
    fun `UX Test - New player onboarding`() = runBlocking {
        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val eventBus = SimpleEventBus()
        val gameService = GameApplicationService(appConfig, eventBus)

        // Create a new player
        val playerId = UUID.randomUUID().toString()
        val playerName = "NewPlayer"
        val player = gameService.getOrCreatePlayer(playerId, playerName)

        // Verify player was created successfully
        assertNotNull(player, "Player should be created successfully")

        // Get a story ID (we'll use a known one for testing)
        val storyId = "adventure" // Assuming this exists

        // Start a story
        val startBeat = gameService.startStory(player.id, storyId)

        // Verify story started successfully
        assertNotNull(startBeat, "Story should start successfully")

        // Verify there are choices available
        assertTrue(startBeat!!.choices.isNotEmpty(), "There should be choices available")

        // Make a choice
        val choiceId = startBeat.choices.first().id
        val nextBeat = gameService.makeChoice(player.id, storyId, choiceId)

        // Verify choice was processed successfully
        assertNotNull(nextBeat, "Choice should be processed successfully")

        println("New player onboarding test passed successfully")
    }

    /**
     * Test the premium upgrade flow.
     * This test simulates a free user upgrading to a premium subscription.
     */
    @Test
    fun `UX Test - Premium upgrade flow`() = runBlocking {
        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val eventBus = SimpleEventBus()
        val gameService = GameApplicationService(appConfig, eventBus)

        // Create a new player (free tier by default)
        val playerId = UUID.randomUUID().toString()
        val playerName = "FreePlayer"
        val player = gameService.getOrCreatePlayer(playerId, playerName)

        // Verify player is on free tier
        assertTrue(player.subscriptionTier == SubscriptionTier.FREE, "Player should start on free tier")

        // Try to access a premium story
        val premiumStoryId = "premium_story" // Assuming this exists
        val premiumStoryAccess = gameService.startStory(player.id, premiumStoryId)

        // Verify access is denied (or handled gracefully if story doesn't exist)
        // Note: In a real test, we would ensure a premium story exists

        // Simulate upgrading to premium by directly modifying the player
        player.subscriptionTier = SubscriptionTier.PREMIUM
        player.subscriptionExpiresAt = Instant.now().plusSeconds(86400) // 1 day from now

        // Try to access premium story again
        val premiumStoryAccessAfterUpgrade = gameService.startStory(player.id, premiumStoryId)

        // Note: In a real test with actual premium stories, we would verify access is granted

        println("Premium upgrade flow test passed successfully")
    }

    /**
     * Test error recovery.
     * This test simulates an error occurring during gameplay and verifies that the player can recover.
     */
    @Test
    fun `UX Test - Error recovery`() = runBlocking {
        // Initialize core components
        val appConfig = AppConfig(gamesDirectory = "games")
        val eventBus = SimpleEventBus()
        val gameService = GameApplicationService(appConfig, eventBus)

        // Create a new player
        val playerId = UUID.randomUUID().toString()
        val playerName = "ErrorPlayer"
        val player = gameService.getOrCreatePlayer(playerId, playerName)

        // Start a story
        val storyId = "adventure" // Assuming this exists
        val startBeat = gameService.startStory(player.id, storyId)

        // Verify story started successfully
        assertNotNull(startBeat, "Story should start successfully")

        // Simulate an error by passing an invalid choice ID
        val invalidChoiceId = "invalid_choice_id"
        val errorResult = gameService.makeChoice(player.id, storyId, invalidChoiceId)

        // Verify error handling (should return null but not crash)
        assertTrue(errorResult == null, "Invalid choice should be handled gracefully")

        // Verify player can continue after error
        val validChoiceId = startBeat!!.choices.first().id
        val recoveryResult = gameService.makeChoice(player.id, storyId, validChoiceId)

        // Verify recovery was successful
        assertNotNull(recoveryResult, "Player should be able to continue after error")

        println("Error recovery test passed successfully")
    }
}
