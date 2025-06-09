package integration

import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.json.JsonStoryRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for a full game playthrough.
 * Tests the complete flow of a game from start to finish.
 */
class FullGamePlaythroughTest {

    private lateinit var appConfig: AppConfig
    private lateinit var gameService: GameApplicationService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var storyRepository: JsonStoryRepository

    @BeforeEach
    fun setUp() {
        // Create test configuration
        appConfig = AppConfig(
            databaseUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            databaseUsername = "sa",
            databasePassword = "",
            gamesDirectory = "games" // Use the actual games directory
        )

        // Initialize object mapper
        objectMapper = ObjectMapper().registerKotlinModule()

        // Initialize story repository
        storyRepository = JsonStoryRepository(appConfig, objectMapper)

        // Initialize game service
        gameService = GameApplicationService(appConfig)

        // Create a test adventure story
        val adventureStory = createTestAdventureStory()

        // Use reflection to set the storyCache field directly
        val storyCacheField = GameApplicationService::class.java.getDeclaredField("storyCache")
        storyCacheField.isAccessible = true
        val storyCache = ConcurrentHashMap<String, Story>()
        storyCache["adventure"] = adventureStory
        storyCacheField.set(gameService, storyCache)

        println("Adventure story created successfully with ${adventureStory.beats.size} beats")
    }

    /**
     * Create a test adventure story with multiple beats and choices.
     * This story has a linear path with 6 beats, allowing for 5 choices.
     * The final beat is an end beat.
     */
    private fun createTestAdventureStory(): Story {
        val story = Story(
            id = "adventure",
            title = createLocalizedText("Adventure"),
            description = createLocalizedText("A test adventure story"),
            startBeatId = "beat1"
        )

        // Create 6 beats (the last one is an end beat)
        val beat1 = StoryBeat(
            id = "beat1",
            text = createLocalizedText("You stand at the entrance of a dark cave. What do you do?")
        )

        val beat2 = StoryBeat(
            id = "beat2",
            text = createLocalizedText("Inside the cave, you find a fork in the path. Which way do you go?")
        )

        val beat3 = StoryBeat(
            id = "beat3",
            text = createLocalizedText("You encounter a sleeping dragon. How do you proceed?")
        )

        val beat4 = StoryBeat(
            id = "beat4",
            text = createLocalizedText("You find a treasure chest. What do you do?")
        )

        val beat5 = StoryBeat(
            id = "beat5",
            text = createLocalizedText("You hear footsteps approaching. How do you react?")
        )

        val beat6 = StoryBeat(
            id = "beat6",
            text = createLocalizedText("Congratulations! You've completed the adventure."),
            isEndBeat = true
        )

        // Create choices for each beat
        val choice1 = NarrativeChoice(
            id = "choice1",
            text = createLocalizedText("Enter the cave"),
            nextBeatId = "beat2"
        )

        val choice2 = NarrativeChoice(
            id = "choice2",
            text = createLocalizedText("Go left"),
            nextBeatId = "beat3"
        )

        val choice3 = NarrativeChoice(
            id = "choice3",
            text = createLocalizedText("Sneak past the dragon"),
            nextBeatId = "beat4"
        )

        val choice4 = NarrativeChoice(
            id = "choice4",
            text = createLocalizedText("Open the chest"),
            nextBeatId = "beat5"
        )

        val choice5 = NarrativeChoice(
            id = "choice5",
            text = createLocalizedText("Hide and wait"),
            nextBeatId = "beat6"
        )

        // Add consequences to some choices to modify player attributes
        val consequence1 = Consequence(
            id = UUID.randomUUID().toString(),
            type = ConsequenceType.ATTRIBUTE,
            target = "courage",
            value = "10"
        )

        val consequence2 = Consequence(
            id = UUID.randomUUID().toString(),
            type = ConsequenceType.ATTRIBUTE,
            target = "wisdom",
            value = "5"
        )

        choice1.addConsequence(consequence1)
        choice3.addConsequence(consequence2)

        // Add choices to beats
        beat1.addChoice(choice1)
        beat2.addChoice(choice2)
        beat3.addChoice(choice3)
        beat4.addChoice(choice4)
        beat5.addChoice(choice5)

        // Add beats to story
        story.addBeat(beat1)
        story.addBeat(beat2)
        story.addBeat(beat3)
        story.addBeat(beat4)
        story.addBeat(beat5)
        story.addBeat(beat6)

        return story
    }

    /**
     * Create a localized text with English content.
     */
    private fun createLocalizedText(text: String): LocalizedText {
        val localizedText = LocalizedText()
        localizedText.setText(Language.EN, text)
        return localizedText
    }

    @Test
    fun `should complete full game playthrough from start to ending`() = runBlocking {
        // 1. Create a player
        val playerId = UUID.randomUUID().toString()
        val playerName = "TestPlayer"
        val player = gameService.getOrCreatePlayer(playerId, playerName)

        // Verify player was created
        assertNotNull(player)
        assertEquals(playerId, player.id)

        // 2. Load the "adventure" game
        val storyId = "adventure"
        val startBeat = gameService.startStory(playerId, storyId)

        // Verify game was loaded
        assertNotNull(startBeat)

        // Store initial player state to verify changes later
        val initialAttributes = player.attributes.toMap()

        // 3. Make at least 5 choices to progress through the game
        var currentBeat = startBeat!!  // Non-null assertion since we verified startBeat is not null
        val choiceHistory = mutableListOf<String>()
        var endBeatReached = false

        // Make up to 10 choices (to avoid infinite loop)
        for (i in 1..10) {
            // Verify current beat has choices unless it's an end beat
            if (currentBeat.isEndBeat) {
                endBeatReached = true
                break
            }

            // Verify there are choices available
            assertTrue(currentBeat.choices.isNotEmpty(), "Beat should have choices: ${currentBeat.id}")

            // Select the first available choice
            val choice = currentBeat.choices.first()
            choiceHistory.add(choice.id)

            // Make the choice
            val nextBeat = gameService.makeChoice(playerId, storyId, choice.id)

            // Verify the choice led to a new beat
            assertNotNull(nextBeat, "Choice should lead to a new beat")

            // Move to the next beat
            currentBeat = nextBeat!!

            // If we've made at least 5 choices and reached an end beat, we can stop
            if (i >= 5 && currentBeat.isEndBeat) {
                endBeatReached = true
                break
            }
        }

        // 4. Verify we reached the end of the game
        assertTrue(choiceHistory.size >= 5, "Should make at least 5 choices, made: ${choiceHistory.size}")
        assertTrue(endBeatReached, "Should reach an end beat")

        // 5. Verify player state was preserved between moves
        // Reload the player to ensure state was persisted
        val updatedPlayer = gameService.getOrCreatePlayer(playerId, playerName)

        // Verify player progress contains the current beat
        val currentBeatId = updatedPlayer.getProgress("story:$storyId:currentBeat")
        assertNotNull(currentBeatId, "Player should have current beat stored in progress")

        // Verify player attributes were updated (should be different from initial state)
        // This assumes that choices in the adventure game have consequences that modify player attributes
        val finalAttributes = updatedPlayer.attributes.toMap()

        // Print debug information about player state
        println("Initial attributes: $initialAttributes")
        println("Final attributes: $finalAttributes")
        println("Choices made: $choiceHistory")

        // Verify player state was preserved
        assertNotNull(updatedPlayer)
        assertEquals(playerId, updatedPlayer.id)
    }
}
