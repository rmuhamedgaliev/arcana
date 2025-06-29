package io.github.rmuhamedgaliev.arcana.application.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.rmuhamedgaliev.arcana.application.events.AbstractEvent
import io.github.rmuhamedgaliev.arcana.application.events.SimpleEventBus
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.domain.ports.PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2DatabaseConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.json.JsonStoryRepository
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Game-related events
 */
object GameEvents {
    /**
     * Event fired when a story is started
     */
    data class StoryStarted(val playerId: String, val storyId: String) : AbstractEvent() {
        override fun getType(): String = "StoryStarted"
    }

    /**
     * Event fired when a choice is made
     */
    data class ChoiceMade(val playerId: String, val storyId: String, val choiceId: String) : AbstractEvent() {
        override fun getType(): String = "ChoiceMade"
    }
}

/**
 * Application service for game-related operations.
 * This is the main entry point for game functionality.
 */
class GameApplicationService(
    private val config: AppConfig,
    private val eventBus: SimpleEventBus = SimpleEventBus()
) {
    private val dbConfig = H2DatabaseConfig(config)
    private val playerRepository: PlayerRepository = H2PlayerRepository(dbConfig)

    // In-memory cache of loaded stories
    private val storyCache = ConcurrentHashMap<String, Story>()

    private val objectMapper = jacksonObjectMapper()
    private val storyRepository = JsonStoryRepository(config, objectMapper)

    init {
        // Initialize database
        dbConfig.initialize()
        loadStories()
    }

    private fun loadStories() {
        runBlocking {
            try {
                logger.info { "Loading stories from: ${config.gamesDirectory}" }
                val stories = storyRepository.findAll()
                stories.forEach { story ->
                    storyCache[story.id] = story
                    logger.info { "Loaded story: ${story.id}" }
                }
                logger.info { "Total stories loaded: ${stories.size}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load stories" }
            }
        }
    }

    /**
     * Get or create a player.
     *
     * @param playerId The player ID
     * @param name The player name (stored as an attribute)
     * @return The player
     */
    suspend fun getOrCreatePlayer(playerId: String, name: String): Player {
        return playerRepository.findById(playerId) ?: run {
            val player = Player(id = playerId)
            player.setAttribute("name", name.hashCode()) // Store name as a hashed attribute
            playerRepository.save(player)
            player
        }
    }

    /**
     * Start a story for a player.
     *
     * @param playerId The player ID
     * @param storyId The story ID
     * @return The starting beat of the story
     */
    suspend fun startStory(playerId: String, storyId: String): StoryBeat? {
        logger.info { "Starting story '$storyId' for player $playerId" }

        val player = playerRepository.findById(playerId) ?: return null
        val story = getStory(storyId) ?: run {
            logger.error { "Story '$storyId' not found" }
            return null
        }

        logger.info { "Story '$storyId' loaded successfully: ${story.title}" }

        val startBeat = story.getBeat(story.startBeatId) ?: return null

        // Record that player started this story
        setCurrentBeat(player, storyId, story.startBeatId)
        playerRepository.save(player)

        // Publish event
        eventBus.publish(GameEvents.StoryStarted(playerId, storyId))

        return startBeat
    }

    /**
     * Make a choice in a story.
     *
     * @param playerId The player ID
     * @param storyId The story ID
     * @param choiceId The choice ID
     * @return The next beat, or null if the choice is invalid
     */
    suspend fun makeChoice(playerId: String, storyId: String, choiceId: String): StoryBeat? {
        val player = playerRepository.findById(playerId) ?: return null
        val story = getStory(storyId) ?: return null

        // Get current beat
        val currentBeatId = getCurrentBeatId(player, storyId) ?: story.startBeatId
        val currentBeat = story.getBeat(currentBeatId) ?: return null

        // Find the choice
        val choice = currentBeat.choices.find { it.id == choiceId } ?: return null

        // Check if choice is valid
        if (choice.hasCondition()) {
            val conditionMet = evaluateCondition(player, story, choice.condition!!)
            if (!conditionMet) {
                logger.info { "Choice condition not met: ${choice.condition}" }
                return null
            }
        }

        // Get next beat
        val nextBeat = story.getBeat(choice.nextBeatId) ?: return null

        // Apply immediate consequences
        choice.getImmediateConsequences().forEach { consequence ->
            applyConsequence(player, story, consequence)
        }

        // Update player's current beat
        setCurrentBeat(player, storyId, nextBeat.id)
        playerRepository.save(player)

        // Publish event
        eventBus.publish(GameEvents.ChoiceMade(playerId, storyId, choiceId))

        return nextBeat
    }

    /**
     * Get a story by ID.
     *
     * @param storyId The story ID
     * @return The story, or null if not found
     */
    private fun getStory(storyId: String): Story? {
        logger.info { "Attempting to load story: $storyId" }

        // Check cache first
        return storyCache[storyId] ?: run {
            logger.warn { "Story '$storyId' not found in cache. Available stories: ${storyCache.keys}" }
            // TODO: Implement StoryRepository to load from JSON files
            null
        }
    }

    /**
     * Evaluate a condition.
     *
     * @param player The player
     * @param story The story
     * @param condition The condition to evaluate
     * @return True if the condition is met, false otherwise
     */
    private fun evaluateCondition(player: Player, story: Story, condition: String): Boolean {
        // TODO: Implement condition evaluation
        // For now, return true
        return true
    }

    /**
     * Apply a consequence.
     *
     * @param player The player
     * @param story The story
     * @param consequence The consequence to apply
     */
    private fun applyConsequence(player: Player, story: Story, consequence: Consequence) {
        // TODO: Implement consequence application
    }

    /**
     * Check if a player has premium access.
     *
     * @param player The player
     * @param requiredTier The required subscription tier
     * @return True if the player has premium access, false otherwise
     */
    private fun hasPremiumAccess(player: Player, requiredTier: SubscriptionTier): Boolean {
        return player.hasActiveSubscription() && player.subscriptionTier.ordinal >= requiredTier.ordinal
    }

    /**
     * Get the current beat ID for a player in a story.
     *
     * @param player The player
     * @param storyId The story ID
     * @return The current beat ID, or null if not set
     */
    private fun getCurrentBeatId(player: Player, storyId: String): String? {
        return player.getProgress("story:$storyId:currentBeat")
    }

    /**
     * Set the current beat for a player in a story.
     *
     * @param player The player
     * @param storyId The story ID
     * @param beatId The beat ID
     */
    private fun setCurrentBeat(player: Player, storyId: String, beatId: String) {
        player.setProgress("story:$storyId:currentBeat", beatId)
    }
}
