package io.github.rmuhamedgaliev.arcana.unit.domain.model.world

import io.github.rmuhamedgaliev.arcana.domain.model.world.Rumor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RumorTest {

    @Test
    fun `should create rumor with correct properties`() {
        // Given
        val id = "rumor1"
        val text = "The king is planning to raise taxes"
        val source = "Town crier"
        val truthValue = 75
        val spreadFactor = 60

        // When
        val rumor = Rumor(id, text, source, truthValue, spreadFactor)

        // Then
        assertEquals(id, rumor.id)
        assertEquals(text, rumor.text)
        assertEquals(source, rumor.source)
        assertEquals(truthValue, rumor.truthValue)
        assertEquals(spreadFactor, rumor.spreadFactor)
    }

    @Test
    fun `should generate UUID if id not provided`() {
        // When
        val rumor = Rumor(
            text = "The king is planning to raise taxes",
            source = "Town crier",
            truthValue = 75,
            spreadFactor = 60
        )

        // Then
        assertNotNull(rumor.id)
    }

    @Test
    fun `should serialize rumor`() {
        // Given
        val rumor = Rumor(
            id = "rumor1",
            text = "The king is planning to raise taxes",
            source = "Town crier",
            truthValue = 75,
            spreadFactor = 60
        )

        // When
        val serialized = rumor.serialize()

        // Then
        assertEquals("rumor1|The king is planning to raise taxes|Town crier|75|60", serialized)
    }

    @Test
    fun `should deserialize rumor`() {
        // Given
        val serialized = "rumor1|The king is planning to raise taxes|Town crier|75|60"

        // When
        val rumor = Rumor.deserialize(serialized)

        // Then
        assertEquals("rumor1", rumor.id)
        assertEquals("The king is planning to raise taxes", rumor.text)
        assertEquals("Town crier", rumor.source)
        assertEquals(75, rumor.truthValue)
        assertEquals(60, rumor.spreadFactor)
    }

    @Test
    fun `should handle invalid serialized data`() {
        // Given
        val invalidSerialized = "rumor1"

        // When
        val rumor = Rumor.deserialize(invalidSerialized)

        // Then
        assertEquals("Unknown rumor", rumor.text)
        assertEquals("Unknown", rumor.source)
        assertEquals(50, rumor.truthValue)
        assertEquals(50, rumor.spreadFactor)
    }

    @Test
    fun `should handle serialized data with invalid numbers`() {
        // Given
        val invalidSerialized = "rumor1|The king is planning to raise taxes|Town crier|invalid|invalid"

        // When
        val rumor = Rumor.deserialize(invalidSerialized)

        // Then
        assertEquals("rumor1", rumor.id)
        assertEquals("The king is planning to raise taxes", rumor.text)
        assertEquals("Town crier", rumor.source)
        assertEquals(50, rumor.truthValue) // Default value
        assertEquals(50, rumor.spreadFactor) // Default value
    }
}
