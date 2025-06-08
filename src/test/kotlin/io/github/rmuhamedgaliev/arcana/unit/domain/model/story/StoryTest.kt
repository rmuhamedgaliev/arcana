package io.github.rmuhamedgaliev.arcana.unit.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryArc
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoryTest {

    private lateinit var title: LocalizedText
    private lateinit var description: LocalizedText
    private lateinit var startBeat: StoryBeat
    private lateinit var secondBeat: StoryBeat
    private lateinit var mainArc: StoryArc
    private lateinit var sideArc: StoryArc
    private lateinit var dependentArc: StoryArc

    @BeforeEach
    fun setUp() {
        title = LocalizedText().apply {
            setText(Language.EN, "The Dark Cave")
            setText(Language.RU, "Темная пещера")
        }

        description = LocalizedText().apply {
            setText(Language.EN, "Explore a mysterious cave filled with danger and treasure.")
            setText(Language.RU, "Исследуйте таинственную пещеру, полную опасностей и сокровищ.")
        }

        startBeat = StoryBeat(
            "start",
            LocalizedText().apply {
                setText(Language.EN, "You stand at the entrance of a dark cave.")
                setText(Language.RU, "Вы стоите у входа в темную пещеру.")
            }
        )

        secondBeat = StoryBeat(
            "cave_interior",
            LocalizedText().apply {
                setText(Language.EN, "Inside the cave, it's dark and damp.")
                setText(Language.RU, "Внутри пещеры темно и сыро.")
            }
        )

        val arcDescription = LocalizedText().apply {
            setText(Language.EN, "Description of the arc")
            setText(Language.RU, "Описание арки")
        }

        mainArc = StoryArc(
            "main_quest",
            LocalizedText().apply {
                setText(Language.EN, "Main Quest")
                setText(Language.RU, "Основной квест")
            },
            arcDescription,
            null,
            setOf(),
            setOf(),
            "start"
        )

        sideArc = StoryArc(
            "treasure_hunt",
            LocalizedText().apply {
                setText(Language.EN, "Treasure Hunt")
                setText(Language.RU, "Охота за сокровищами")
            },
            arcDescription,
            null,
            setOf(),
            setOf(),
            "cave_interior"
        )

        dependentArc = StoryArc(
            "rescue_mission",
            LocalizedText().apply {
                setText(Language.EN, "Rescue Mission")
                setText(Language.RU, "Спасательная миссия")
            },
            arcDescription,
            "main_quest",
            setOf(),
            setOf(),
            "cave_interior"
        )
    }

    @Test
    fun `should create story with correct properties`() {
        // Given
        val id = "dark_cave"

        // When
        val story = Story(id, title, description, "start")

        // Then
        assertEquals(id, story.id)
        assertEquals(title, story.title)
        assertEquals(description, story.description)
        assertEquals("start", story.startBeatId)
        assertTrue(story.beats.isEmpty())
        assertTrue(story.arcs.isEmpty())
        assertEquals(SubscriptionTier.FREE, story.requiredSubscriptionTier)
        assertTrue(story.tags.isEmpty())
        assertTrue(story.metadata.isEmpty())
    }

    @Test
    fun `should add and retrieve beats`() {
        // Given
        val story = Story("dark_cave", title, description, "start")

        // When
        story.addBeat(startBeat)
        story.addBeat(secondBeat)

        // Then
        assertEquals(2, story.beats.size)
        assertEquals(startBeat, story.getBeat("start"))
        assertEquals(secondBeat, story.getBeat("cave_interior"))
        assertNull(story.getBeat("nonexistent"))
    }

    @Test
    fun `should add and retrieve arcs`() {
        // Given
        val story = Story("dark_cave", title, description, "start")

        // When
        story.addArc(mainArc)
        story.addArc(sideArc)
        story.addArc(dependentArc)

        // Then
        assertEquals(3, story.arcs.size)
        assertTrue(story.arcs.contains(mainArc))
        assertTrue(story.arcs.contains(sideArc))
        assertTrue(story.arcs.contains(dependentArc))
    }

    @Test
    fun `should get available arcs based on unlocked arcs`() {
        // Given
        val story = Story("dark_cave", title, description, "start")
        story.addArc(mainArc)
        story.addArc(sideArc)
        story.addArc(dependentArc)

        // When - no arcs unlocked
        val noUnlockedArcs = story.getAvailableArcs(emptySet())

        // Then
        assertEquals(2, noUnlockedArcs.size)
        assertTrue(noUnlockedArcs.contains(mainArc))
        assertTrue(noUnlockedArcs.contains(sideArc))
        assertFalse(noUnlockedArcs.contains(dependentArc))

        // When - main quest unlocked
        val mainQuestUnlocked = story.getAvailableArcs(setOf("main_quest"))

        // Then
        assertEquals(3, mainQuestUnlocked.size)
        assertTrue(mainQuestUnlocked.contains(mainArc))
        assertTrue(mainQuestUnlocked.contains(sideArc))
        assertTrue(mainQuestUnlocked.contains(dependentArc))
    }

    @Test
    fun `should check if story is premium content`() {
        // Given
        val freeStory = Story("free_story", title, description, "start", requiredSubscriptionTier = SubscriptionTier.FREE)
        val premiumStory = Story("premium_story", title, description, "start", requiredSubscriptionTier = SubscriptionTier.PREMIUM)

        // Then
        assertFalse(freeStory.isPremium())
        assertTrue(premiumStory.isPremium())
    }

    @Test
    fun `should add and retrieve metadata`() {
        // Given
        val story = Story("dark_cave", title, description, "start")

        // When
        story.addMetadata("author", "John Doe")
        story.addMetadata("version", "1.0")

        // Then
        assertEquals("John Doe", story.getMetadata("author"))
        assertEquals("1.0", story.getMetadata("version"))
        assertNull(story.getMetadata("nonexistent"))
    }

    @Test
    fun `should add and check tags`() {
        // Given
        val story = Story("dark_cave", title, description, "start")

        // When
        story.addTag("adventure")
        story.addTag("fantasy")

        // Then
        assertTrue(story.hasTag("adventure"))
        assertTrue(story.hasTag("fantasy"))
        assertFalse(story.hasTag("sci-fi"))
    }

    @Test
    fun `should handle conditional arc dependencies`() {
        // Given
        val story = Story("dark_cave", title, description, "start")
        val arcDescription = LocalizedText().apply {
            setText(Language.EN, "Description of the arc")
            setText(Language.RU, "Описание арки")
        }
        val conditionalArc = StoryArc(
            "hidden_path",
            LocalizedText().apply {
                setText(Language.EN, "Hidden Path")
                setText(Language.RU, "Скрытый путь")
            },
            arcDescription,
            "condition:has_map == true",
            setOf(),
            setOf(),
            "cave_interior"
        )
        story.addArc(conditionalArc)

        // When
        val availableArcs = story.getAvailableArcs(emptySet())

        // Then
        assertEquals(1, availableArcs.size)
        assertTrue(availableArcs.contains(conditionalArc))
    }
}
