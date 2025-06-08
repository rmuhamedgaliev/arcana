package io.github.rmuhamedgaliev.arcana.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText

/**
 * Class representing a story beat.
 * A story beat is a single scene or moment in the story.
 */
data class StoryBeat(
    val id: String,
    val text: LocalizedText,
    val choices: MutableList<NarrativeChoice> = mutableListOf(),
    val isEndBeat: Boolean = false,
    val attributes: MutableMap<String, String> = mutableMapOf(),
    val tags: MutableSet<String> = mutableSetOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Add a choice to the beat.
     *
     * @param choice The choice to add
     */
    fun addChoice(choice: NarrativeChoice) {
        choices.add(choice)
    }

    /**
     * Get valid choices based on conditions.
     *
     * @param conditionEvaluator A function that evaluates conditions
     * @return The list of valid choices
     */
    fun getValidChoices(conditionEvaluator: (String) -> Boolean): List<NarrativeChoice> {
        return choices.filter { choice ->
            !choice.hasCondition() || conditionEvaluator(choice.condition!!)
        }
    }

    /**
     * Add an attribute to the beat.
     *
     * @param key The attribute key
     * @param value The attribute value
     */
    fun addAttribute(key: String, value: String) {
        attributes[key] = value
    }

    /**
     * Get an attribute from the beat.
     *
     * @param key The attribute key
     * @return The attribute value, or null if not found
     */
    fun getAttribute(key: String): String? {
        return attributes[key]
    }

    /**
     * Add metadata to the beat.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the beat.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }

    /**
     * Add a tag to the beat.
     *
     * @param tag The tag to add
     */
    fun addTag(tag: String) {
        tags.add(tag)
    }

    /**
     * Check if the beat has a tag.
     *
     * @param tag The tag to check
     * @return True if the beat has the tag, false otherwise
     */
    fun hasTag(tag: String): Boolean {
        return tags.contains(tag)
    }
}
