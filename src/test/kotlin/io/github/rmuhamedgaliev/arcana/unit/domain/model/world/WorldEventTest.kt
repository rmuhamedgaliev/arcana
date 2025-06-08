package io.github.rmuhamedgaliev.arcana.unit.domain.model.world

import io.github.rmuhamedgaliev.arcana.domain.model.world.WorldEvent
import io.github.rmuhamedgaliev.arcana.domain.model.world.WorldEventType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WorldEventTest {

    @Test
    fun `should create world event with correct properties`() {
        // Given
        val id = "event1"
        val name = "Drought"
        val description = "A severe drought has affected the kingdom"
        val type = WorldEventType.ENVIRONMENTAL
        val impact = 80
        val duration = 5

        // When
        val worldEvent = WorldEvent(id, name, description, type, impact, duration)

        // Then
        assertEquals(id, worldEvent.id)
        assertEquals(name, worldEvent.name)
        assertEquals(description, worldEvent.description)
        assertEquals(type, worldEvent.type)
        assertEquals(impact, worldEvent.impact)
        assertEquals(duration, worldEvent.duration)
    }

    @Test
    fun `should generate UUID if id not provided`() {
        // When
        val worldEvent = WorldEvent(
            name = "Drought",
            description = "A severe drought has affected the kingdom",
            type = WorldEventType.ENVIRONMENTAL,
            impact = 80,
            duration = 5
        )

        // Then
        assertNotNull(worldEvent.id)
    }

    @Test
    fun `should serialize world event`() {
        // Given
        val worldEvent = WorldEvent(
            id = "event1",
            name = "Drought",
            description = "A severe drought has affected the kingdom",
            type = WorldEventType.ENVIRONMENTAL,
            impact = 80,
            duration = 5
        )

        // When
        val serialized = worldEvent.serialize()

        // Then
        assertEquals("event1|Drought|A severe drought has affected the kingdom|ENVIRONMENTAL|80|5", serialized)
    }

    @Test
    fun `should deserialize world event`() {
        // Given
        val serialized = "event1|Drought|A severe drought has affected the kingdom|ENVIRONMENTAL|80|5"

        // When
        val worldEvent = WorldEvent.deserialize(serialized)

        // Then
        assertEquals("event1", worldEvent.id)
        assertEquals("Drought", worldEvent.name)
        assertEquals("A severe drought has affected the kingdom", worldEvent.description)
        assertEquals(WorldEventType.ENVIRONMENTAL, worldEvent.type)
        assertEquals(80, worldEvent.impact)
        assertEquals(5, worldEvent.duration)
    }

    @Test
    fun `should handle invalid serialized data`() {
        // Given
        val invalidSerialized = "event1"

        // When
        val worldEvent = WorldEvent.deserialize(invalidSerialized)

        // Then
        assertEquals("Unknown event", worldEvent.name)
        assertEquals("Unknown", worldEvent.description)
        assertEquals(WorldEventType.POLITICAL, worldEvent.type)
        assertEquals(50, worldEvent.impact)
        assertEquals(3, worldEvent.duration)
    }

    @Test
    fun `should handle serialized data with invalid numbers`() {
        // Given
        val invalidSerialized = "event1|Drought|A severe drought has affected the kingdom|ENVIRONMENTAL|invalid|invalid"

        // When
        val worldEvent = WorldEvent.deserialize(invalidSerialized)

        // Then
        assertEquals("event1", worldEvent.id)
        assertEquals("Drought", worldEvent.name)
        assertEquals("A severe drought has affected the kingdom", worldEvent.description)
        assertEquals(WorldEventType.ENVIRONMENTAL, worldEvent.type)
        assertEquals(50, worldEvent.impact) // Default value
        assertEquals(3, worldEvent.duration) // Default value
    }

    @Test
    fun `should handle serialized data with invalid event type`() {
        // Given
        val invalidSerialized = "event1|Drought|A severe drought has affected the kingdom|INVALID_TYPE|80|5"

        // When
        val worldEvent = WorldEvent.deserialize(invalidSerialized)

        // Then
        assertEquals("event1", worldEvent.id)
        assertEquals("Drought", worldEvent.name)
        assertEquals("A severe drought has affected the kingdom", worldEvent.description)
        assertEquals(WorldEventType.POLITICAL, worldEvent.type) // Default value
        assertEquals(80, worldEvent.impact)
        assertEquals(5, worldEvent.duration)
    }

    @Test
    fun `should support all event types`() {
        // Then
        assertEquals(5, WorldEventType.values().size)
        assertEquals(WorldEventType.POLITICAL, WorldEventType.valueOf("POLITICAL"))
        assertEquals(WorldEventType.ENVIRONMENTAL, WorldEventType.valueOf("ENVIRONMENTAL"))
        assertEquals(WorldEventType.SOCIAL, WorldEventType.valueOf("SOCIAL"))
        assertEquals(WorldEventType.ECONOMIC, WorldEventType.valueOf("ECONOMIC"))
        assertEquals(WorldEventType.MILITARY, WorldEventType.valueOf("MILITARY"))
    }
}
