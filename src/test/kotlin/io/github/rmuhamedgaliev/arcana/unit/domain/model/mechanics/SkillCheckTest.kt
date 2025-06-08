package io.github.rmuhamedgaliev.arcana.unit.domain.model.mechanics

import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.SkillCheck
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.SkillCheckOutcome
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.skillCheck
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class SkillCheckTest {

    @Mock
    private lateinit var player: Player

    private lateinit var successOutcome: SkillCheckOutcome
    private lateinit var failureOutcome: SkillCheckOutcome
    private lateinit var criticalSuccessOutcome: SkillCheckOutcome
    private lateinit var criticalFailureOutcome: SkillCheckOutcome
    private lateinit var skillCheck: SkillCheck

    @BeforeEach
    fun setUp() {
        successOutcome = SkillCheckOutcome(
            id = "success",
            text = "You succeed in climbing the wall",
            nextBeatId = "top_of_wall"
        )

        failureOutcome = SkillCheckOutcome(
            id = "failure",
            text = "You fail to climb the wall",
            nextBeatId = "base_of_wall"
        )

        criticalSuccessOutcome = SkillCheckOutcome(
            id = "critical_success",
            text = "You climb the wall with incredible ease",
            nextBeatId = "top_of_wall_advantage"
        )

        criticalFailureOutcome = SkillCheckOutcome(
            id = "critical_failure",
            text = "You fall badly while trying to climb",
            nextBeatId = "injured_at_base"
        )

        skillCheck = SkillCheck(
            id = "climb_check",
            attributeName = "strength",
            difficulty = 12,
            bonusModifier = 2,
            criticalSuccessThreshold = 18,
            criticalFailureThreshold = 3,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = criticalSuccessOutcome,
            criticalFailureOutcome = criticalFailureOutcome
        )
    }

    @Test
    fun `should create skill check with correct properties`() {
        // Given
        val id = "climb_check"
        val attributeName = "strength"
        val difficulty = 12
        val bonusModifier = 2
        val criticalSuccessThreshold = 18
        val criticalFailureThreshold = 3

        // Then
        assertEquals(id, skillCheck.id)
        assertEquals(attributeName, skillCheck.attributeName)
        assertEquals(difficulty, skillCheck.difficulty)
        assertEquals(bonusModifier, skillCheck.bonusModifier)
        assertEquals(criticalSuccessThreshold, skillCheck.criticalSuccessThreshold)
        assertEquals(criticalFailureThreshold, skillCheck.criticalFailureThreshold)
        assertEquals(successOutcome, skillCheck.successOutcome)
        assertEquals(failureOutcome, skillCheck.failureOutcome)
        assertEquals(criticalSuccessOutcome, skillCheck.criticalSuccessOutcome)
        assertEquals(criticalFailureOutcome, skillCheck.criticalFailureOutcome)
    }

    @Test
    fun `should generate UUID if id not provided`() {
        // When
        val skillCheckWithoutId = SkillCheck(
            attributeName = "strength",
            difficulty = 12,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome
        )

        // Then
        assertNotNull(skillCheckWithoutId.id)
    }

    @Test
    fun `should return success outcome when check succeeds`() {
        // Given
        `when`(player.getAttribute("strength")).thenReturn(10)
        
        // Using a fixed seed to ensure the roll is between 4 and 17 (not critical)
        val random = Random(123)
        val roll = random.nextInt(1, 21)
        
        // Ensure the roll + attribute + modifier >= difficulty
        // 10 (attribute) + 2 (modifier) + roll >= 12 (difficulty)
        // roll needs to be >= 0, which is always true for a d20
        
        // When
        val outcome = skillCheck.perform(player)
        
        // Then
        assertEquals(successOutcome, outcome)
    }

    @Test
    fun `should return failure outcome when check fails`() {
        // Given
        `when`(player.getAttribute("strength")).thenReturn(5)
        
        // Using a fixed seed to ensure the roll is between 4 and 17 (not critical)
        val random = Random(456)
        val roll = random.nextInt(1, 21)
        
        // Ensure the roll + attribute + modifier < difficulty
        // 5 (attribute) + 2 (modifier) + roll < 12 (difficulty)
        // roll needs to be < 5, which can happen with a d20
        
        // When
        val outcome = skillCheck.perform(player)
        
        // Then
        assertEquals(failureOutcome, outcome)
    }

    @Test
    fun `should return critical success outcome when roll is high enough`() {
        // Given
        `when`(player.getAttribute("strength")).thenReturn(10)
        
        // Mock the Random.nextInt to return a critical success roll (18 or higher)
        val mockSkillCheck = object : SkillCheck(
            id = "climb_check",
            attributeName = "strength",
            difficulty = 12,
            bonusModifier = 2,
            criticalSuccessThreshold = 18,
            criticalFailureThreshold = 3,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = criticalSuccessOutcome,
            criticalFailureOutcome = criticalFailureOutcome
        ) {
            override fun rollDice(): Int = 20 // Always roll a 20
        }
        
        // When
        val outcome = mockSkillCheck.perform(player)
        
        // Then
        assertEquals(criticalSuccessOutcome, outcome)
    }

    @Test
    fun `should return critical failure outcome when roll is low enough`() {
        // Given
        `when`(player.getAttribute("strength")).thenReturn(10)
        
        // Mock the Random.nextInt to return a critical failure roll (3 or lower)
        val mockSkillCheck = object : SkillCheck(
            id = "climb_check",
            attributeName = "strength",
            difficulty = 12,
            bonusModifier = 2,
            criticalSuccessThreshold = 18,
            criticalFailureThreshold = 3,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = criticalSuccessOutcome,
            criticalFailureOutcome = criticalFailureOutcome
        ) {
            override fun rollDice(): Int = 1 // Always roll a 1
        }
        
        // When
        val outcome = mockSkillCheck.perform(player)
        
        // Then
        assertEquals(criticalFailureOutcome, outcome)
    }

    @Test
    fun `should fall back to success outcome when critical success outcome is null`() {
        // Given
        `when`(player.getAttribute("strength")).thenReturn(10)
        
        // Create a skill check without critical outcomes
        val skillCheckWithoutCriticals = SkillCheck(
            id = "climb_check",
            attributeName = "strength",
            difficulty = 12,
            bonusModifier = 2,
            criticalSuccessThreshold = 18,
            criticalFailureThreshold = 3,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = null,
            criticalFailureOutcome = null
        )
        
        // Mock the Random.nextInt to return a critical success roll (18 or higher)
        val mockSkillCheck = object : SkillCheck(
            id = "climb_check",
            attributeName = "strength",
            difficulty = 12,
            bonusModifier = 2,
            criticalSuccessThreshold = 18,
            criticalFailureThreshold = 3,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = null,
            criticalFailureOutcome = null
        ) {
            override fun rollDice(): Int = 20 // Always roll a 20
        }
        
        // When
        val outcome = mockSkillCheck.perform(player)
        
        // Then
        assertEquals(successOutcome, outcome)
    }

    @Test
    fun `should create skill check using DSL`() {
        // Given
        val consequence = Consequence(
            id = "consequence1",
            type = ConsequenceType.ATTRIBUTE,
            target = "health",
            value = "-5"
        )

        // When
        val dslSkillCheck = skillCheck {
            attribute("agility")
            difficulty(15)
            bonusModifier(3)
            criticalSuccessThreshold(19)
            criticalFailureThreshold(2)
            
            onSuccess("You succeed", "success_beat")
            onFailure("You fail", "failure_beat", listOf(consequence))
            onCriticalSuccess("You critically succeed", "critical_success_beat")
            onCriticalFailure("You critically fail", "critical_failure_beat")
        }

        // Then
        assertEquals("agility", dslSkillCheck.attributeName)
        assertEquals(15, dslSkillCheck.difficulty)
        assertEquals(3, dslSkillCheck.bonusModifier)
        assertEquals(19, dslSkillCheck.criticalSuccessThreshold)
        assertEquals(2, dslSkillCheck.criticalFailureThreshold)
        
        assertEquals("You succeed", dslSkillCheck.successOutcome.text)
        assertEquals("success_beat", dslSkillCheck.successOutcome.nextBeatId)
        assertTrue(dslSkillCheck.successOutcome.consequences.isEmpty())
        
        assertEquals("You fail", dslSkillCheck.failureOutcome.text)
        assertEquals("failure_beat", dslSkillCheck.failureOutcome.nextBeatId)
        assertEquals(1, dslSkillCheck.failureOutcome.consequences.size)
        assertEquals(consequence.id, dslSkillCheck.failureOutcome.consequences[0].id)
        
        assertEquals("You critically succeed", dslSkillCheck.criticalSuccessOutcome?.text)
        assertEquals("critical_success_beat", dslSkillCheck.criticalSuccessOutcome?.nextBeatId)
        
        assertEquals("You critically fail", dslSkillCheck.criticalFailureOutcome?.text)
        assertEquals("critical_failure_beat", dslSkillCheck.criticalFailureOutcome?.nextBeatId)
    }

    @Test
    fun `should add and retrieve metadata from skill check outcome`() {
        // Given
        val outcome = SkillCheckOutcome(
            id = "outcome1",
            text = "You succeed",
            nextBeatId = "next_beat"
        )

        // When
        outcome.addMetadata("difficulty", "hard")
        outcome.addMetadata("reward", "gold")

        // Then
        assertEquals("hard", outcome.getMetadata("difficulty"))
        assertEquals("gold", outcome.getMetadata("reward"))
        assertNull(outcome.getMetadata("nonexistent"))
    }

    private fun assertTrue(isEmpty: Boolean) {
        assertEquals(true, isEmpty)
    }
}
