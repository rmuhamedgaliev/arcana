package io.github.rmuhamedgaliev.arcana.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ChoiceWeight
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence

/**
 * Class representing a narrative choice in a story beat.
 */
data class NarrativeChoice(
    val id: String,
    val text: LocalizedText,
    val nextBeatId: String,
    val condition: String? = null,
    val weight: ChoiceWeight = ChoiceWeight.NORMAL,
    val consequences: MutableList<Consequence> = mutableListOf(),
    val tags: MutableSet<String> = mutableSetOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Check if this choice has a condition.
     *
     * @return True if this choice has a condition, false otherwise
     */
    fun hasCondition(): Boolean {
        return condition != null && condition.isNotBlank()
    }

    /**
     * Add a consequence to the choice.
     *
     * @param consequence The consequence to add
     */
    fun addConsequence(consequence: Consequence) {
        consequences.add(consequence)
    }

    /**
     * Get immediate consequences.
     *
     * @return The list of immediate consequences
     */
    fun getImmediateConsequences(): List<Consequence> {
        return consequences.filter { it.isImmediate() }
    }

    /**
     * Get delayed consequences.
     *
     * @return The list of delayed consequences
     */
    fun getDelayedConsequences(): List<Consequence> {
        return consequences.filter { it.isDelayed() }
    }

    /**
     * Add metadata to the choice.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the choice.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }

    /**
     * Add a tag to the choice.
     *
     * @param tag The tag to add
     */
    fun addTag(tag: String) {
        tags.add(tag)
    }

    /**
     * Check if the choice has a tag.
     *
     * @param tag The tag to check
     * @return True if the choice has the tag, false otherwise
     */
    fun hasTag(tag: String): Boolean {
        return tags.contains(tag)
    }
}
