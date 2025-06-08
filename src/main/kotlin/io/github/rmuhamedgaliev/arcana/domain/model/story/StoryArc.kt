package io.github.rmuhamedgaliev.arcana.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText

/**
 * Class representing a story arc.
 * A story arc is a collection of related story beats that form a coherent narrative segment.
 */
data class StoryArc(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val dependency: String? = null,
    val exclusiveWith: Set<String> = setOf(),
    val unlocks: Set<String> = setOf(),
    val startBeatId: String,
    val endBeatIds: Set<String> = setOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Check if this arc is exclusive with another arc.
     *
     * @param arcId The ID of the arc to check
     * @return True if this arc is exclusive with the specified arc, false otherwise
     */
    fun isExclusiveWith(arcId: String): Boolean {
        return exclusiveWith.contains(arcId)
    }

    /**
     * Check if this arc unlocks another arc.
     *
     * @param arcId The ID of the arc to check
     * @return True if this arc unlocks the specified arc, false otherwise
     */
    fun unlocks(arcId: String): Boolean {
        return unlocks.contains(arcId)
    }

    /**
     * Add metadata to the arc.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the arc.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }
}
