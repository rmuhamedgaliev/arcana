package io.github.rmuhamedgaliev.arcana.unit.domain.model.mechanics

import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConsequenceTest {

    @Test
    fun `should create consequence with correct properties`() {
        // Given
        val id = "consequence1"
        val type = ConsequenceType.ATTRIBUTE
        val target = "health"
        val value = "+10"
        val delay = 0
        val condition = "health < 50"

        // When
        val consequence = Consequence(id, type, target, value, delay, condition)

        // Then
        assertEquals(id, consequence.id)
        assertEquals(type, consequence.type)
        assertEquals(target, consequence.target)
        assertEquals(value, consequence.value)
        assertEquals(delay, consequence.delay)
        assertEquals(condition, consequence.condition)
    }

    @Test
    fun `should identify immediate consequences`() {
        // Given
        val immediateConsequence = Consequence("c1", ConsequenceType.ATTRIBUTE, "health", "+10", 0)
        val negativeDelayConsequence = Consequence("c2", ConsequenceType.ATTRIBUTE, "health", "+10", -1)
        val delayedConsequence = Consequence("c3", ConsequenceType.ATTRIBUTE, "health", "+10", 2)

        // Then
        assertTrue(immediateConsequence.isImmediate())
        assertTrue(negativeDelayConsequence.isImmediate())
        assertFalse(delayedConsequence.isImmediate())
    }

    @Test
    fun `should identify delayed consequences`() {
        // Given
        val immediateConsequence = Consequence("c1", ConsequenceType.ATTRIBUTE, "health", "+10", 0)
        val negativeDelayConsequence = Consequence("c2", ConsequenceType.ATTRIBUTE, "health", "+10", -1)
        val delayedConsequence = Consequence("c3", ConsequenceType.ATTRIBUTE, "health", "+10", 2)

        // Then
        assertFalse(immediateConsequence.isDelayed())
        assertFalse(negativeDelayConsequence.isDelayed())
        assertTrue(delayedConsequence.isDelayed())
    }

    @Test
    fun `should identify consequences with conditions`() {
        // Given
        val consequenceWithCondition = Consequence("c1", ConsequenceType.ATTRIBUTE, "health", "+10", 0, "health < 50")
        val consequenceWithEmptyCondition = Consequence("c2", ConsequenceType.ATTRIBUTE, "health", "+10", 0, "")
        val consequenceWithoutCondition = Consequence("c3", ConsequenceType.ATTRIBUTE, "health", "+10", 0, null)

        // Then
        assertTrue(consequenceWithCondition.hasCondition())
        assertFalse(consequenceWithEmptyCondition.hasCondition())
        assertFalse(consequenceWithoutCondition.hasCondition())
    }

    @Test
    fun `should add and retrieve metadata`() {
        // Given
        val consequence = Consequence("c1", ConsequenceType.ATTRIBUTE, "health", "+10")
        
        // When
        consequence.addMetadata("source", "poison")
        consequence.addMetadata("severity", "high")
        
        // Then
        assertEquals("poison", consequence.getMetadata("source"))
        assertEquals("high", consequence.getMetadata("severity"))
        assertEquals(null, consequence.getMetadata("nonexistent"))
    }

    @Test
    fun `should support all consequence types`() {
        // Given/When/Then - Just verifying all types are available
        assertEquals(ConsequenceType.ATTRIBUTE, ConsequenceType.valueOf("ATTRIBUTE"))
        assertEquals(ConsequenceType.EVENT, ConsequenceType.valueOf("EVENT"))
        assertEquals(ConsequenceType.RELATIONSHIP, ConsequenceType.valueOf("RELATIONSHIP"))
        assertEquals(ConsequenceType.FACTION, ConsequenceType.valueOf("FACTION"))
        assertEquals(ConsequenceType.WORLD_STATE, ConsequenceType.valueOf("WORLD_STATE"))
        assertEquals(ConsequenceType.CHAIN_REACTION, ConsequenceType.valueOf("CHAIN_REACTION"))
        assertEquals(ConsequenceType.CUMULATIVE, ConsequenceType.valueOf("CUMULATIVE"))
    }
}
