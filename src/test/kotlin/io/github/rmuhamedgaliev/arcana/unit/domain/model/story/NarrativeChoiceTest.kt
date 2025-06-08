package io.github.rmuhamedgaliev.arcana.unit.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ChoiceWeight
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NarrativeChoiceTest {

    private lateinit var localizedText: LocalizedText
    private lateinit var immediateConsequence: Consequence
    private lateinit var delayedConsequence: Consequence

    @BeforeEach
    fun setUp() {
        localizedText = LocalizedText().apply {
            setText(Language.EN, "Go to the castle")
            setText(Language.RU, "Идти в замок")
        }

        immediateConsequence = Consequence(
            "c1",
            ConsequenceType.ATTRIBUTE,
            "courage",
            "+5",
            0
        )

        delayedConsequence = Consequence(
            "c2",
            ConsequenceType.RELATIONSHIP,
            "king",
            "-10",
            3
        )
    }

    @Test
    fun `should create narrative choice with correct properties`() {
        // Given
        val id = "choice1"
        val nextBeatId = "castle_entrance"
        val condition = "has_sword == true"
        val weight = ChoiceWeight.PROMINENT

        // When
        val choice = NarrativeChoice(id, localizedText, nextBeatId, condition, weight)

        // Then
        assertEquals(id, choice.id)
        assertEquals(localizedText, choice.text)
        assertEquals(nextBeatId, choice.nextBeatId)
        assertEquals(condition, choice.condition)
        assertEquals(weight, choice.weight)
        assertTrue(choice.consequences.isEmpty())
        assertTrue(choice.tags.isEmpty())
        assertTrue(choice.metadata.isEmpty())
    }

    @Test
    fun `should identify choices with conditions`() {
        // Given
        val choiceWithCondition = NarrativeChoice("c1", localizedText, "next", "has_sword == true")
        val choiceWithEmptyCondition = NarrativeChoice("c2", localizedText, "next", "")
        val choiceWithoutCondition = NarrativeChoice("c3", localizedText, "next", null)

        // Then
        assertTrue(choiceWithCondition.hasCondition())
        assertFalse(choiceWithEmptyCondition.hasCondition())
        assertFalse(choiceWithoutCondition.hasCondition())
    }

    @Test
    fun `should add and retrieve consequences`() {
        // Given
        val choice = NarrativeChoice("c1", localizedText, "next")

        // When
        choice.addConsequence(immediateConsequence)
        choice.addConsequence(delayedConsequence)

        // Then
        assertEquals(2, choice.consequences.size)
        assertTrue(choice.consequences.contains(immediateConsequence))
        assertTrue(choice.consequences.contains(delayedConsequence))
    }

    @Test
    fun `should get immediate consequences`() {
        // Given
        val choice = NarrativeChoice("c1", localizedText, "next")
        choice.addConsequence(immediateConsequence)
        choice.addConsequence(delayedConsequence)

        // When
        val immediateConsequences = choice.getImmediateConsequences()

        // Then
        assertEquals(1, immediateConsequences.size)
        assertEquals(immediateConsequence, immediateConsequences[0])
    }

    @Test
    fun `should get delayed consequences`() {
        // Given
        val choice = NarrativeChoice("c1", localizedText, "next")
        choice.addConsequence(immediateConsequence)
        choice.addConsequence(delayedConsequence)

        // When
        val delayedConsequences = choice.getDelayedConsequences()

        // Then
        assertEquals(1, delayedConsequences.size)
        assertEquals(delayedConsequence, delayedConsequences[0])
    }

    @Test
    fun `should add and retrieve metadata`() {
        // Given
        val choice = NarrativeChoice("c1", localizedText, "next")

        // When
        choice.addMetadata("difficulty", "hard")
        choice.addMetadata("requires", "sword")

        // Then
        assertEquals("hard", choice.getMetadata("difficulty"))
        assertEquals("sword", choice.getMetadata("requires"))
        assertEquals(null, choice.getMetadata("nonexistent"))
    }

    @Test
    fun `should add and check tags`() {
        // Given
        val choice = NarrativeChoice("c1", localizedText, "next")

        // When
        choice.addTag("brave")
        choice.addTag("risky")

        // Then
        assertTrue(choice.hasTag("brave"))
        assertTrue(choice.hasTag("risky"))
        assertFalse(choice.hasTag("safe"))
    }
}
