package unit.application.services

import io.github.rmuhamedgaliev.arcana.application.events.SimpleEventBus
import io.github.rmuhamedgaliev.arcana.application.services.GameApplicationService
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.domain.ports.PlayerRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import io.github.rmuhamedgaliev.arcana.infrastructure.database.H2PlayerRepository
import io.mockk.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameApplicationServiceTest {

    // Mocks
    private lateinit var playerRepository: PlayerRepository
    private lateinit var appConfig: AppConfig
    private lateinit var eventBus: SimpleEventBus

    // System under test
    private lateinit var gameService: GameApplicationService

    // Test data
    private val playerId = "player1"
    private val playerName = "Test Player"
    private val storyId = "story1"
    private val beatId = "beat1"
    private val choiceId = "choice1"
    private val invalidChoiceId = "invalid_choice"

    @BeforeEach
    fun setUp() {
        // Initialize mocks
        playerRepository = mockk()
        appConfig = mockk(relaxed = true)
        eventBus = mockk(relaxed = true)

        // Mock AppConfig properties needed for database initialization
        every { appConfig.databaseUrl } returns "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        every { appConfig.databaseUsername } returns "sa"
        every { appConfig.databasePassword } returns ""

        // Mock the H2PlayerRepository constructor to return our mock repository
        mockkConstructor(H2PlayerRepository::class)
        coEvery { anyConstructed<H2PlayerRepository>().findById(any()) } coAnswers { 
            playerRepository.findById(firstArg()) 
        }
        coEvery { anyConstructed<H2PlayerRepository>().save(any()) } coAnswers { 
            playerRepository.save(firstArg()) 
        }

        // Create the GameApplicationService with our mocks
        gameService = GameApplicationService(appConfig, eventBus)

        // Use reflection to set the storyCache field directly
        val storyCacheField = GameApplicationService::class.java.getDeclaredField("storyCache")
        storyCacheField.isAccessible = true
        val storyCache = ConcurrentHashMap<String, Story>()
        storyCache[storyId] = createTestStory()
        storyCache["premium_story"] = createPremiumStory()
        storyCacheField.set(gameService, storyCache)
    }

    @Test
    fun `should create player successfully`() {
        // Given
        val player = Player(id = playerId)
        coEvery { playerRepository.findById(playerId) } returns null
        coEvery { playerRepository.save(any()) } returns player

        // When
        val result = runBlocking { gameService.getOrCreatePlayer(playerId, playerName) }

        // Then
        assertNotNull(result)
        assertEquals(playerId, result.id)
        runBlocking { coVerify { playerRepository.save(any()) } }
    }

    @Test
    fun `should start story for valid player and story`() {
        // Given
        val player = Player(id = playerId)
        coEvery { playerRepository.findById(playerId) } returns player
        coEvery { playerRepository.save(any()) } returns player

        // When
        val result = runBlocking { gameService.startStory(playerId, storyId) }

        // Then
        assertNotNull(result)
        assertEquals(beatId, result.id)
        runBlocking { coVerify { playerRepository.save(any()) } }
        verify { eventBus.publish(any()) }
    }

    @Test
    fun `should return null when starting non-existent story`() {
        // Given
        val player = Player(id = playerId)
        coEvery { playerRepository.findById(playerId) } returns player

        // When
        val result = runBlocking { gameService.startStory(playerId, "nonexistent_story") }

        // Then
        assertNull(result)
    }

    @Test
    fun `should make choice and return next beat`() {
        // Given
        val player = Player(id = playerId)
        player.setProgress("story:$storyId:currentBeat", beatId)
        coEvery { playerRepository.findById(playerId) } returns player
        coEvery { playerRepository.save(any()) } returns player

        // When
        val result = runBlocking { gameService.makeChoice(playerId, storyId, choiceId) }

        // Then
        assertNotNull(result)
        assertEquals("beat2", result.id)
        runBlocking { coVerify { playerRepository.save(any()) } }
        verify { eventBus.publish(any()) }
    }

    @Test
    fun `should return null for invalid choice`() {
        // Given
        val player = Player(id = playerId)
        player.setProgress("story:$storyId:currentBeat", beatId)
        coEvery { playerRepository.findById(playerId) } returns player

        // When
        val result = runBlocking { gameService.makeChoice(playerId, storyId, invalidChoiceId) }

        // Then
        assertNull(result)
    }

    @Test
    fun `should check premium access correctly`() {
        // Given
        val freePlayer = Player(id = "free_player", subscriptionTier = SubscriptionTier.FREE)
        val basicPlayer = Player(
            id = "basic_player", 
            subscriptionTier = SubscriptionTier.BASIC,
            subscriptionExpiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        )
        val premiumStory = createPremiumStory()

        coEvery { playerRepository.findById("free_player") } returns freePlayer
        coEvery { playerRepository.findById("basic_player") } returns basicPlayer

        // When - Free player tries to access premium story
        val freeResult = runBlocking { gameService.startStory("free_player", "premium_story") }

        // Then
        assertNull(freeResult)

        // When - Basic player tries to access premium story that requires BASIC tier
        coEvery { playerRepository.save(any()) } returns basicPlayer
        val basicResult = runBlocking { gameService.startStory("basic_player", "premium_story") }

        // Then
        assertNotNull(basicResult)
    }

    // Helper methods to create test data
    private fun createTestStory(): Story {
        val story = Story(
            id = storyId,
            title = createLocalizedText("Test Story"),
            description = createLocalizedText("A test story"),
            startBeatId = beatId
        )

        val beat1 = StoryBeat(
            id = beatId,
            text = createLocalizedText("This is the first beat")
        )

        val beat2 = StoryBeat(
            id = "beat2",
            text = createLocalizedText("This is the second beat")
        )

        val choice = NarrativeChoice(
            id = choiceId,
            text = createLocalizedText("Go to the next beat"),
            nextBeatId = "beat2"
        )

        beat1.addChoice(choice)
        story.addBeat(beat1)
        story.addBeat(beat2)

        return story
    }

    private fun createPremiumStory(): Story {
        val story = Story(
            id = "premium_story",
            title = createLocalizedText("Premium Story"),
            description = createLocalizedText("A premium story"),
            startBeatId = "premium_beat",
            requiredSubscriptionTier = SubscriptionTier.BASIC
        )

        val beat = StoryBeat(
            id = "premium_beat",
            text = createLocalizedText("This is a premium beat")
        )

        story.addBeat(beat)

        return story
    }

    private fun createLocalizedText(text: String): LocalizedText {
        val localizedText = LocalizedText()
        localizedText.setText(Language.EN, text)
        return localizedText
    }
}
