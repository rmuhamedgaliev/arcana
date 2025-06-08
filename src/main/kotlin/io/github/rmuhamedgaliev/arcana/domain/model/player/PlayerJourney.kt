package io.github.rmuhamedgaliev.arcana.domain.model.player

import java.time.Instant

/**
 * Class representing a player's journey through a story.
 * Tracks the player's progress, choices, and unlocked arcs.
 */
data class PlayerJourney(
    val id: String,
    val playerId: String,
    val storyId: String,
    val currentBeatId: String,
    val startedAt: Instant = Instant.now(),
    val lastUpdatedAt: Instant = Instant.now(),
    var completedAt: Instant? = null,
    val choices: MutableList<JourneyChoice> = mutableListOf(),
    val unlockedArcs: MutableSet<String> = mutableSetOf(),
    val visitedBeats: MutableSet<String> = mutableSetOf(),
    val attributes: MutableMap<String, Int> = mutableMapOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Record a choice made by the player.
     *
     * @param choiceId The ID of the choice
     * @param beatId The ID of the beat where the choice was made
     * @param timestamp The timestamp when the choice was made
     */
    fun recordChoice(choiceId: String, beatId: String, timestamp: Instant = Instant.now()) {
        choices.add(JourneyChoice(choiceId, beatId, timestamp))
        visitedBeats.add(beatId)
    }

    /**
     * Unlock a story arc.
     *
     * @param arcId The ID of the arc to unlock
     */
    fun unlockArc(arcId: String) {
        unlockedArcs.add(arcId)
    }

    /**
     * Check if an arc is unlocked.
     *
     * @param arcId The ID of the arc to check
     * @return True if the arc is unlocked, false otherwise
     */
    fun isArcUnlocked(arcId: String): Boolean {
        return unlockedArcs.contains(arcId)
    }

    /**
     * Check if a beat has been visited.
     *
     * @param beatId The ID of the beat to check
     * @return True if the beat has been visited, false otherwise
     */
    fun hasVisitedBeat(beatId: String): Boolean {
        return visitedBeats.contains(beatId)
    }

    /**
     * Set the current beat.
     *
     * @param beatId The ID of the beat
     */
    fun setCurrentBeat(beatId: String) {
        visitedBeats.add(beatId)
    }

    /**
     * Complete the journey.
     *
     * @param timestamp The timestamp when the journey was completed
     */
    fun complete(timestamp: Instant = Instant.now()) {
        completedAt = timestamp
    }

    /**
     * Check if the journey is completed.
     *
     * @return True if the journey is completed, false otherwise
     */
    fun isCompleted(): Boolean {
        return completedAt != null
    }

    /**
     * Set an attribute.
     *
     * @param key The attribute key
     * @param value The attribute value
     */
    fun setAttribute(key: String, value: Int) {
        attributes[key] = value
    }

    /**
     * Get an attribute.
     *
     * @param key The attribute key
     * @return The attribute value, or 0 if not set
     */
    fun getAttribute(key: String): Int {
        return attributes.getOrDefault(key, 0)
    }

    /**
     * Add metadata to the journey.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the journey.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }
}

/**
 * Class representing a choice made by a player during their journey.
 */
data class JourneyChoice(
    val choiceId: String,
    val beatId: String,
    val timestamp: Instant = Instant.now()
)
