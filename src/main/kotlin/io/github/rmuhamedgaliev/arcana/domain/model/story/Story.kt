package io.github.rmuhamedgaliev.arcana.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier

/**
 * Class representing a story/quest.
 */
data class Story(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val startBeatId: String,
    val beats: MutableMap<String, StoryBeat> = mutableMapOf(),
    val arcs: MutableList<StoryArc> = mutableListOf(),
    val requiredSubscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val tags: MutableSet<String> = mutableSetOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Add a beat to the story.
     *
     * @param beat The beat to add
     */
    fun addBeat(beat: StoryBeat) {
        beats[beat.id] = beat
    }

    /**
     * Get a beat by its ID.
     *
     * @param beatId The beat ID
     * @return The beat, or null if not found
     */
    fun getBeat(beatId: String): StoryBeat? {
        return beats[beatId]
    }

    /**
     * Add a story arc to the story.
     *
     * @param arc The arc to add
     */
    fun addArc(arc: StoryArc) {
        arcs.add(arc)
    }

    /**
     * Get all arcs that are currently available based on dependencies.
     *
     * @param unlockedArcs The IDs of arcs that have been unlocked
     * @return The available arcs
     */
    fun getAvailableArcs(unlockedArcs: Set<String>): List<StoryArc> {
        return arcs.filter { arc ->
            // If the arc has no dependency, it's always available
            arc.dependency.isNullOrEmpty() ||
            // If the dependency is an arc ID, check if it's unlocked
            unlockedArcs.contains(arc.dependency) ||
            // If the dependency is a condition, it will be evaluated elsewhere
            arc.dependency.startsWith("condition:")
        }
    }

    /**
     * Check if the story is premium content.
     *
     * @return True if the story is premium content, false otherwise
     */
    fun isPremium(): Boolean {
        return requiredSubscriptionTier != SubscriptionTier.FREE
    }

    /**
     * Add metadata to the story.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the story.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }

    /**
     * Add a tag to the story.
     *
     * @param tag The tag to add
     */
    fun addTag(tag: String) {
        tags.add(tag)
    }

    /**
     * Check if the story has a tag.
     *
     * @param tag The tag to check
     * @return True if the story has the tag, false otherwise
     */
    fun hasTag(tag: String): Boolean {
        return tags.contains(tag)
    }
}
