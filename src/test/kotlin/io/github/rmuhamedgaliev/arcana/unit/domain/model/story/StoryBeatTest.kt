package io.github.rmuhamedgaliev.arcana.unit.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryBeatTest {

    private lateinit var localizedText: LocalizedText
    private lateinit var choice1: NarrativeChoice
    private lateinit var choice2: NarrativeChoice
    private lateinit var conditionalChoice: NarrativeChoice

    @BeforeEach
    fun setUp() {
        localizedText = LocalizedText().apply {
            setText(Language.EN, "You stand at the entrance of a dark cave.")
            setText(Language.RU, "Вы стоите у входа в темную пещеру.")
        }

        choice1 = NarrativeChoice(
            "choice1",
            LocalizedText().apply {
                setText(Language.EN, "Enter the cave")
                setText(Language.RU, "Войти в пещеру")
            },
            "cave_interior"
        )

        choice2 = NarrativeChoice(
            "choice2",
            LocalizedText().apply {
                setText(Language.EN, "Walk away")
                setText(Language.RU, "Уйти")
            },
            "forest_path"
        )

        conditionalChoice = NarrativeChoice(
            "choice3",
            LocalizedText().apply {
                setText(Language.EN, "Light a torch")
                setText(Language.RU, "Зажечь факел")
            },
            "cave_lit",
            "has_torch == true"
        )
    }

    @Test
    fun `should create story beat with correct properties`() {
        // Given
        val id = "cave_entrance"
        val isEndBeat = false

        // When
        val beat = StoryBeat(id, localizedText, mutableListOf(), isEndBeat)

        // Then
        assertEquals(id, beat.id)
        assertEquals(localizedText, beat.text)
        assertTrue(beat.choices.isEmpty())
        assertEquals(isEndBeat, beat.isEndBeat)
        assertTrue(beat.attributes.isEmpty())
        assertTrue(beat.tags.isEmpty())
        assertTrue(beat.metadata.isEmpty())
    }

    @Test
    fun `should add and retrieve choices`() {
        // Given
        val beat = StoryBeat("cave_entrance", localizedText)

        // When
        beat.addChoice(choice1)
        beat.addChoice(choice2)

        // Then
        assertEquals(2, beat.choices.size)
        assertTrue(beat.choices.contains(choice1))
        assertTrue(beat.choices.contains(choice2))
    }

    @Test
    fun `should get valid choices based on condition evaluator`() {
        // Given
        val beat = StoryBeat("cave_entrance", localizedText)
        beat.addChoice(choice1)
        beat.addChoice(choice2)
        beat.addChoice(conditionalChoice)

        // When - condition evaluator that always returns true
        val allChoices = beat.getValidChoices { true }

        // Then
        assertEquals(3, allChoices.size)
        assertTrue(allChoices.contains(choice1))
        assertTrue(allChoices.contains(choice2))
        assertTrue(allChoices.contains(conditionalChoice))

        // When - condition evaluator that always returns false
        val nonConditionalChoices = beat.getValidChoices { false }

        // Then
        assertEquals(2, nonConditionalChoices.size)
        assertTrue(nonConditionalChoices.contains(choice1))
        assertTrue(nonConditionalChoices.contains(choice2))
        assertFalse(nonConditionalChoices.contains(conditionalChoice))

        // When - specific condition evaluator
        val specificConditionChoices = beat.getValidChoices { condition ->
            condition == "has_torch == true"
        }

        // Then
        assertEquals(3, specificConditionChoices.size)
        assertTrue(specificConditionChoices.contains(choice1))
        assertTrue(specificConditionChoices.contains(choice2))
        assertTrue(specificConditionChoices.contains(conditionalChoice))
    }

    @Test
    fun `should add and retrieve attributes`() {
        // Given
        val beat = StoryBeat("cave_entrance", localizedText)

        // When
        beat.addAttribute("mood", "dark")
        beat.addAttribute("danger", "high")

        // Then
        assertEquals("dark", beat.getAttribute("mood"))
        assertEquals("high", beat.getAttribute("danger"))
        assertEquals(null, beat.getAttribute("nonexistent"))
    }

    @Test
    fun `should add and retrieve metadata`() {
        // Given
        val beat = StoryBeat("cave_entrance", localizedText)

        // When
        beat.addMetadata("author", "John Doe")
        beat.addMetadata("version", "1.0")

        // Then
        assertEquals("John Doe", beat.getMetadata("author"))
        assertEquals("1.0", beat.getMetadata("version"))
        assertEquals(null, beat.getMetadata("nonexistent"))
    }

    @Test
    fun `should add and check tags`() {
        // Given
        val beat = StoryBeat("cave_entrance", localizedText)

        // When
        beat.addTag("dark")
        beat.addTag("dangerous")

        // Then
        assertTrue(beat.hasTag("dark"))
        assertTrue(beat.hasTag("dangerous"))
        assertFalse(beat.hasTag("safe"))
    }
}
