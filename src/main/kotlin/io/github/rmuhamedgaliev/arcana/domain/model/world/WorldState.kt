package io.github.rmuhamedgaliev.arcana.domain.model.world

import java.util.*

/**
 * Class representing the state of the game world.
 * Tracks political, environmental, and social aspects of the world.
 */
data class WorldState(
    val id: String = UUID.randomUUID().toString(),
    val storyId: String,
    val political: PoliticalState = PoliticalState(),
    val environmental: EnvironmentalState = EnvironmentalState(),
    val social: SocialState = SocialState(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Add metadata to the world state.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the world state.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }

    /**
     * Check if the world state has a metadata key.
     *
     * @param key The metadata key
     * @return True if the world state has the metadata key, false otherwise
     */
    fun hasMetadata(key: String): Boolean {
        return metadata.containsKey(key)
    }

    /**
     * Get a serialized representation of the world state.
     * This can be used to store the world state in a database.
     *
     * @return A map of key-value pairs representing the world state
     */
    fun serialize(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Add political state
        result["political.kingdomStability"] = political.kingdomStability.toString()
        result["political.warThreat"] = political.warThreat.toString()
        result["political.economicCondition"] = political.economicCondition.toString()

        // Add environmental state
        result["environmental.seasonalEvents"] = environmental.seasonalEvents.toString()
        result["environmental.weatherEffects"] = environmental.weatherEffects.toString()
        environmental.resourceAvailability.forEach { (resource, amount) ->
            result["environmental.resource.$resource"] = amount.toString()
        }

        // Add social state
        result["social.publicOpinion"] = social.publicOpinion.toString()
        social.rumorMill.forEachIndexed { index, rumor ->
            result["social.rumor.$index"] = rumor.serialize()
        }
        social.currentEvents.forEachIndexed { index, event ->
            result["social.event.$index"] = event.serialize()
        }

        // Add metadata
        metadata.forEach { (key, value) ->
            result["metadata.$key"] = value
        }

        return result
    }

    companion object {
        /**
         * Deserialize a world state from a map of key-value pairs.
         *
         * @param id The world state ID
         * @param storyId The story ID
         * @param data The serialized data
         * @return The deserialized world state
         */
        fun deserialize(id: String, storyId: String, data: Map<String, String>): WorldState {
            val political = PoliticalState(
                kingdomStability = data["political.kingdomStability"]?.toIntOrNull() ?: 50,
                warThreat = data["political.warThreat"]?.toIntOrNull() ?: 20,
                economicCondition = data["political.economicCondition"]?.toIntOrNull() ?: 80
            )

            val environmental = EnvironmentalState(
                seasonalEvents = data["environmental.seasonalEvents"]?.toBoolean() ?: true,
                weatherEffects = data["environmental.weatherEffects"]?.toBoolean() ?: true
            )

            // Add resources
            data.filter { it.key.startsWith("environmental.resource.") }.forEach { (key, value) ->
                val resource = key.removePrefix("environmental.resource.")
                environmental.resourceAvailability[resource] = value.toIntOrNull() ?: 0
            }

            val social = SocialState(
                publicOpinion = data["social.publicOpinion"]?.toIntOrNull() ?: 60
            )

            // Add rumors
            val rumors = mutableListOf<Rumor>()
            var rumorIndex = 0
            while (data.containsKey("social.rumor.$rumorIndex")) {
                val rumorData = data["social.rumor.$rumorIndex"] ?: ""
                rumors.add(Rumor.deserialize(rumorData))
                rumorIndex++
            }
            social.rumorMill.addAll(rumors)

            // Add events
            val events = mutableListOf<WorldEvent>()
            var eventIndex = 0
            while (data.containsKey("social.event.$eventIndex")) {
                val eventData = data["social.event.$eventIndex"] ?: ""
                events.add(WorldEvent.deserialize(eventData))
                eventIndex++
            }
            social.currentEvents.addAll(events)

            // Create world state
            val worldState = WorldState(
                id = id,
                storyId = storyId,
                political = political,
                environmental = environmental,
                social = social
            )

            // Add metadata
            data.filter { it.key.startsWith("metadata.") }.forEach { (key, value) ->
                val metadataKey = key.removePrefix("metadata.")
                worldState.addMetadata(metadataKey, value)
            }

            return worldState
        }
    }
}

/**
 * Class representing the political state of the game world.
 */
data class PoliticalState(
    var kingdomStability: Int = 50,
    var warThreat: Int = 20,
    var economicCondition: Int = 80
)

/**
 * Class representing the environmental state of the game world.
 */
data class EnvironmentalState(
    var seasonalEvents: Boolean = true,
    var weatherEffects: Boolean = true,
    val resourceAvailability: MutableMap<String, Int> = mutableMapOf(
        "food" to 100,
        "gold" to 50
    )
)

/**
 * Class representing the social state of the game world.
 */
data class SocialState(
    var publicOpinion: Int = 60,
    val rumorMill: MutableList<Rumor> = mutableListOf(),
    val currentEvents: MutableList<WorldEvent> = mutableListOf()
)

/**
 * Class representing a rumor in the game world.
 */
data class Rumor(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val source: String,
    val truthValue: Int, // 0-100, where 0 is completely false and 100 is completely true
    val spreadFactor: Int // 0-100, where 0 is not spreading and 100 is spreading rapidly
) {
    /**
     * Serialize the rumor to a string.
     *
     * @return The serialized rumor
     */
    fun serialize(): String {
        return "$id|$text|$source|$truthValue|$spreadFactor"
    }

    companion object {
        /**
         * Deserialize a rumor from a string.
         *
         * @param data The serialized data
         * @return The deserialized rumor
         */
        fun deserialize(data: String): Rumor {
            val parts = data.split("|")
            if (parts.size < 5) {
                return Rumor(
                    text = "Unknown rumor",
                    source = "Unknown",
                    truthValue = 50,
                    spreadFactor = 50
                )
            }

            return Rumor(
                id = parts[0],
                text = parts[1],
                source = parts[2],
                truthValue = parts[3].toIntOrNull() ?: 50,
                spreadFactor = parts[4].toIntOrNull() ?: 50
            )
        }
    }
}

/**
 * Class representing a world event in the game world.
 */
data class WorldEvent(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val type: WorldEventType,
    val impact: Int, // 0-100, where 0 is no impact and 100 is major impact
    val duration: Int // in turns
) {
    /**
     * Serialize the world event to a string.
     *
     * @return The serialized world event
     */
    fun serialize(): String {
        return "$id|$name|$description|${type.name}|$impact|$duration"
    }

    companion object {
        /**
         * Deserialize a world event from a string.
         *
         * @param data The serialized data
         * @return The deserialized world event
         */
        fun deserialize(data: String): WorldEvent {
            val parts = data.split("|")
            if (parts.size < 6) {
                return WorldEvent(
                    name = "Unknown event",
                    description = "Unknown",
                    type = WorldEventType.POLITICAL,
                    impact = 50,
                    duration = 3
                )
            }

            return WorldEvent(
                id = parts[0],
                name = parts[1],
                description = parts[2],
                type = try {
                    WorldEventType.valueOf(parts[3])
                } catch (e: IllegalArgumentException) {
                    WorldEventType.POLITICAL
                },
                impact = parts[4].toIntOrNull() ?: 50,
                duration = parts[5].toIntOrNull() ?: 3
            )
        }
    }
}

/**
 * Enum representing the type of a world event.
 */
enum class WorldEventType {
    POLITICAL,
    ENVIRONMENTAL,
    SOCIAL,
    ECONOMIC,
    MILITARY
}
