package io.github.rmuhamedgaliev.arcana.domain.model.mechanics

/**
 * Class representing a consequence of a player choice.
 * Consequences can be immediate or delayed, and can affect player attributes,
 * trigger events, or modify relationships.
 */
data class Consequence(
    val id: String,
    val type: ConsequenceType,
    val target: String,
    val value: String,
    val delay: Int = 0,
    val condition: String? = null,
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Check if this consequence is immediate (no delay).
     *
     * @return True if this consequence is immediate, false otherwise
     */
    fun isImmediate(): Boolean {
        return delay <= 0
    }

    /**
     * Check if this consequence is delayed.
     *
     * @return True if this consequence is delayed, false otherwise
     */
    fun isDelayed(): Boolean {
        return delay > 0
    }

    /**
     * Check if this consequence has a condition.
     *
     * @return True if this consequence has a condition, false otherwise
     */
    fun hasCondition(): Boolean {
        return condition != null && condition.isNotBlank()
    }

    /**
     * Add metadata to the consequence.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the consequence.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }
}

/**
 * Enum representing the type of a consequence.
 */
enum class ConsequenceType {
    /**
     * Modify a player attribute.
     */
    ATTRIBUTE,
    
    /**
     * Trigger an event.
     */
    EVENT,
    
    /**
     * Modify a relationship with an NPC.
     */
    RELATIONSHIP,
    
    /**
     * Modify a faction standing.
     */
    FACTION,
    
    /**
     * Modify a world state.
     */
    WORLD_STATE,
    
    /**
     * Chain reaction that triggers other consequences.
     */
    CHAIN_REACTION,
    
    /**
     * Cumulative effect that builds up over time.
     */
    CUMULATIVE
}
