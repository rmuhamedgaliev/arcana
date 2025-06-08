package io.github.rmuhamedgaliev.arcana.unit.domain.model.world

import io.github.rmuhamedgaliev.arcana.domain.model.world.EnvironmentalState
import io.github.rmuhamedgaliev.arcana.domain.model.world.PoliticalState
import io.github.rmuhamedgaliev.arcana.domain.model.world.Rumor
import io.github.rmuhamedgaliev.arcana.domain.model.world.SocialState
import io.github.rmuhamedgaliev.arcana.domain.model.world.WorldEvent
import io.github.rmuhamedgaliev.arcana.domain.model.world.WorldEventType
import io.github.rmuhamedgaliev.arcana.domain.model.world.WorldState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorldStateTest {

    private lateinit var worldState: WorldState
    private lateinit var rumor: Rumor
    private lateinit var worldEvent: WorldEvent

    @BeforeEach
    fun setUp() {
        rumor = Rumor(
            id = "rumor1",
            text = "The king is planning to raise taxes",
            source = "Town crier",
            truthValue = 75,
            spreadFactor = 60
        )

        worldEvent = WorldEvent(
            id = "event1",
            name = "Drought",
            description = "A severe drought has affected the kingdom",
            type = WorldEventType.ENVIRONMENTAL,
            impact = 80,
            duration = 5
        )

        val political = PoliticalState(
            kingdomStability = 40,
            warThreat = 30,
            economicCondition = 70
        )

        val environmental = EnvironmentalState(
            seasonalEvents = true,
            weatherEffects = true
        ).apply {
            resourceAvailability["food"] = 80
            resourceAvailability["gold"] = 60
            resourceAvailability["wood"] = 90
        }

        val social = SocialState(
            publicOpinion = 55
        ).apply {
            rumorMill.add(rumor)
            currentEvents.add(worldEvent)
        }

        worldState = WorldState(
            id = "world1",
            storyId = "story1",
            political = political,
            environmental = environmental,
            social = social
        )
    }

    @Test
    fun `should create world state with correct properties`() {
        // Given
        val id = "world1"
        val storyId = "story1"

        // Then
        assertEquals(id, worldState.id)
        assertEquals(storyId, worldState.storyId)
        assertEquals(40, worldState.political.kingdomStability)
        assertEquals(30, worldState.political.warThreat)
        assertEquals(70, worldState.political.economicCondition)
        assertTrue(worldState.environmental.seasonalEvents)
        assertTrue(worldState.environmental.weatherEffects)
        assertEquals(80, worldState.environmental.resourceAvailability["food"])
        assertEquals(60, worldState.environmental.resourceAvailability["gold"])
        assertEquals(90, worldState.environmental.resourceAvailability["wood"])
        assertEquals(55, worldState.social.publicOpinion)
        assertEquals(1, worldState.social.rumorMill.size)
        assertEquals(rumor, worldState.social.rumorMill[0])
        assertEquals(1, worldState.social.currentEvents.size)
        assertEquals(worldEvent, worldState.social.currentEvents[0])
        assertTrue(worldState.metadata.isEmpty())
    }

    @Test
    fun `should add and retrieve metadata`() {
        // When
        worldState.addMetadata("lastUpdated", "2023-01-01")
        worldState.addMetadata("version", "1.0")

        // Then
        assertEquals("2023-01-01", worldState.getMetadata("lastUpdated"))
        assertEquals("1.0", worldState.getMetadata("version"))
        assertEquals(null, worldState.getMetadata("nonexistent"))
    }

    @Test
    fun `should check if world state has metadata`() {
        // Given
        worldState.addMetadata("lastUpdated", "2023-01-01")

        // Then
        assertTrue(worldState.hasMetadata("lastUpdated"))
        assertFalse(worldState.hasMetadata("nonexistent"))
    }

    @Test
    fun `should serialize world state`() {
        // Given
        worldState.addMetadata("lastUpdated", "2023-01-01")

        // When
        val serialized = worldState.serialize()

        // Then
        assertEquals("40", serialized["political.kingdomStability"])
        assertEquals("30", serialized["political.warThreat"])
        assertEquals("70", serialized["political.economicCondition"])
        assertEquals("true", serialized["environmental.seasonalEvents"])
        assertEquals("true", serialized["environmental.weatherEffects"])
        assertEquals("80", serialized["environmental.resource.food"])
        assertEquals("60", serialized["environmental.resource.gold"])
        assertEquals("90", serialized["environmental.resource.wood"])
        assertEquals("55", serialized["social.publicOpinion"])
        assertEquals(rumor.serialize(), serialized["social.rumor.0"])
        assertEquals(worldEvent.serialize(), serialized["social.event.0"])
        assertEquals("2023-01-01", serialized["metadata.lastUpdated"])
    }

    @Test
    fun `should deserialize world state`() {
        // Given
        val serialized = mapOf(
            "political.kingdomStability" to "45",
            "political.warThreat" to "25",
            "political.economicCondition" to "75",
            "environmental.seasonalEvents" to "true",
            "environmental.weatherEffects" to "false",
            "environmental.resource.food" to "85",
            "environmental.resource.gold" to "65",
            "social.publicOpinion" to "60",
            "social.rumor.0" to rumor.serialize(),
            "social.event.0" to worldEvent.serialize(),
            "metadata.lastUpdated" to "2023-01-01"
        )

        // When
        val deserialized = WorldState.deserialize("world2", "story2", serialized)

        // Then
        assertEquals("world2", deserialized.id)
        assertEquals("story2", deserialized.storyId)
        assertEquals(45, deserialized.political.kingdomStability)
        assertEquals(25, deserialized.political.warThreat)
        assertEquals(75, deserialized.political.economicCondition)
        assertTrue(deserialized.environmental.seasonalEvents)
        assertFalse(deserialized.environmental.weatherEffects)
        assertEquals(85, deserialized.environmental.resourceAvailability["food"])
        assertEquals(65, deserialized.environmental.resourceAvailability["gold"])
        assertEquals(60, deserialized.social.publicOpinion)
        assertEquals(1, deserialized.social.rumorMill.size)
        assertEquals(rumor.id, deserialized.social.rumorMill[0].id)
        assertEquals(rumor.text, deserialized.social.rumorMill[0].text)
        assertEquals(1, deserialized.social.currentEvents.size)
        assertEquals(worldEvent.id, deserialized.social.currentEvents[0].id)
        assertEquals(worldEvent.name, deserialized.social.currentEvents[0].name)
        assertEquals("2023-01-01", deserialized.getMetadata("lastUpdated"))
    }

    @Test
    fun `should handle missing fields when deserializing`() {
        // Given
        val minimalSerialized = mapOf<String, String>()

        // When
        val deserialized = WorldState.deserialize("world3", "story3", minimalSerialized)

        // Then
        assertEquals("world3", deserialized.id)
        assertEquals("story3", deserialized.storyId)
        assertEquals(50, deserialized.political.kingdomStability) // Default value
        assertEquals(20, deserialized.political.warThreat) // Default value
        assertEquals(80, deserialized.political.economicCondition) // Default value
        assertTrue(deserialized.environmental.seasonalEvents) // Default value
        assertTrue(deserialized.environmental.weatherEffects) // Default value
        assertEquals(100, deserialized.environmental.resourceAvailability["food"]) // Default value
        assertEquals(50, deserialized.environmental.resourceAvailability["gold"]) // Default value
        assertEquals(60, deserialized.social.publicOpinion) // Default value
        assertTrue(deserialized.social.rumorMill.isEmpty())
        assertTrue(deserialized.social.currentEvents.isEmpty())
        assertTrue(deserialized.metadata.isEmpty())
    }
}
