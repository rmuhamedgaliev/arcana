package io.github.rmuhamedgaliev.arcana.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import java.util.*

/**
 * Class representing an ending in the game.
 * An ending is a special type of story beat that concludes the story.
 */
data class Ending(
    val id: String = UUID.randomUUID().toString(),
    val beatId: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val category: EndingCategory,
    val rarity: EndingRarity,
    val requirements: String? = null,
    val unlocks: MutableMap<String, String> = mutableMapOf(),
    val metadata: MutableMap<String, String> = mutableMapOf(),
    val tags: MutableSet<String> = mutableSetOf()
) {
    /**
     * Add metadata to the ending.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the ending.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }

    /**
     * Add a tag to the ending.
     *
     * @param tag The tag to add
     */
    fun addTag(tag: String) {
        tags.add(tag)
    }

    /**
     * Check if the ending has a tag.
     *
     * @param tag The tag to check
     * @return True if the ending has the tag, false otherwise
     */
    fun hasTag(tag: String): Boolean {
        return tags.contains(tag)
    }

    /**
     * Add an unlock to the ending.
     *
     * @param key The unlock key
     * @param value The unlock value
     */
    fun addUnlock(key: String, value: String) {
        unlocks[key] = value
    }

    /**
     * Get an unlock from the ending.
     *
     * @param key The unlock key
     * @return The unlock value, or null if not found
     */
    fun getUnlock(key: String): String? {
        return unlocks[key]
    }

    /**
     * Check if the ending has an unlock.
     *
     * @param key The unlock key
     * @return True if the ending has the unlock, false otherwise
     */
    fun hasUnlock(key: String): Boolean {
        return unlocks.containsKey(key)
    }
}

/**
 * Enum representing the category of an ending.
 */
enum class EndingCategory {
    HEROIC,
    TRAGIC,
    NEUTRAL,
    EVIL,
    SECRET,
    SPECIAL
}

/**
 * Enum representing the rarity of an ending.
 */
enum class EndingRarity(val percentageOfPlayers: Double) {
    COMMON(50.0),
    UNCOMMON(25.0),
    RARE(15.0),
    VERY_RARE(8.0),
    LEGENDARY(2.0)
}

/**
 * Class for analyzing endings.
 */
class EndingAnalyzer {
    /**
     * Get the dependency graph of endings.
     *
     * @param endings The endings to analyze
     * @return A map of ending IDs to the IDs of endings that depend on them
     */
    fun getDependencyGraph(endings: List<Ending>): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()

        // Initialize the result map
        endings.forEach { ending ->
            result[ending.id] = mutableListOf()
        }

        // Build the dependency graph
        endings.forEach { ending ->
            if (ending.requirements != null) {
                // Parse the requirements to find dependencies
                val dependencies = parseDependencies(ending.requirements)

                // Add the ending to the list of dependents for each dependency
                dependencies.forEach { dependency ->
                    result[dependency]?.add(ending.id)
                }
            }
        }

        return result
    }

    /**
     * Parse the dependencies from a requirements string.
     *
     * @param requirements The requirements string
     * @return A list of ending IDs that the requirements depend on
     */
    private fun parseDependencies(requirements: String): List<String> {
        val dependencies = mutableListOf<String>()

        // Simple parser for requirements
        // In a real implementation, this would be more sophisticated
        val regex = Regex("ending:(\\w+)")
        val matches = regex.findAll(requirements)

        matches.forEach { match ->
            val endingId = match.groupValues[1]
            dependencies.add(endingId)
        }

        return dependencies
    }

    /**
     * Get the rarity distribution of endings.
     *
     * @param endings The endings to analyze
     * @return A map of ending rarity to the number of endings with that rarity
     */
    fun getRarityDistribution(endings: List<Ending>): Map<EndingRarity, Int> {
        val result = mutableMapOf<EndingRarity, Int>()

        // Initialize the result map
        EndingRarity.values().forEach { rarity ->
            result[rarity] = 0
        }

        // Count the number of endings with each rarity
        endings.forEach { ending ->
            result[ending.rarity] = result[ending.rarity]!! + 1
        }

        return result
    }

    /**
     * Get the category distribution of endings.
     *
     * @param endings The endings to analyze
     * @return A map of ending category to the number of endings with that category
     */
    fun getCategoryDistribution(endings: List<Ending>): Map<EndingCategory, Int> {
        val result = mutableMapOf<EndingCategory, Int>()

        // Initialize the result map
        EndingCategory.values().forEach { category ->
            result[category] = 0
        }

        // Count the number of endings with each category
        endings.forEach { ending ->
            result[ending.category] = result[ending.category]!! + 1
        }

        return result
    }

    /**
     * Get hints for undiscovered endings.
     *
     * @param endings The endings to analyze
     * @param discoveredEndingIds The IDs of endings that have been discovered
     * @return A list of hints for undiscovered endings
     */
    fun getHintsForUndiscoveredEndings(endings: List<Ending>, discoveredEndingIds: Set<String>): List<String> {
        val hints = mutableListOf<String>()

        // Get undiscovered endings
        val undiscoveredEndings = endings.filter { !discoveredEndingIds.contains(it.id) }

        // Generate hints for each undiscovered ending
        undiscoveredEndings.forEach { ending ->
            val hint = generateHint(ending, discoveredEndingIds)
            hints.add(hint)
        }

        return hints
    }

    /**
     * Generate a hint for an undiscovered ending.
     *
     * @param ending The ending to generate a hint for
     * @param discoveredEndingIds The IDs of endings that have been discovered
     * @return A hint for the undiscovered ending
     */
    private fun generateHint(ending: Ending, discoveredEndingIds: Set<String>): String {
        // In a real implementation, this would be more sophisticated
        return "Try to find the ${ending.category.name.lowercase()} ending with ${ending.rarity.name.lowercase()} rarity."
    }
}

/**
 * Builder for creating endings.
 */
class EndingBuilder {
    private lateinit var id: String
    private lateinit var beatId: String
    private val title = LocalizedText()
    private val description = LocalizedText()
    private var category: EndingCategory = EndingCategory.NEUTRAL
    private var rarity: EndingRarity = EndingRarity.COMMON
    private var requirements: String? = null
    private val unlocks = mutableMapOf<String, String>()
    private val metadata = mutableMapOf<String, String>()
    private val tags = mutableSetOf<String>()

    /**
     * Set the ID of the ending.
     */
    fun setId(id: String) {
        this.id = id
    }

    /**
     * Set the beat ID of the ending.
     */
    fun setBeatId(beatId: String) {
        this.beatId = beatId
    }

    /**
     * Set the title of the ending.
     */
    fun title(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(title)
    }

    /**
     * Set the description of the ending.
     */
    fun description(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(description)
    }

    /**
     * Set the category of the ending.
     */
    fun category(category: EndingCategory) {
        this.category = category
    }

    /**
     * Set the rarity of the ending.
     */
    fun rarity(rarity: EndingRarity) {
        this.rarity = rarity
    }

    /**
     * Set the requirements for the ending.
     */
    fun requirements(requirements: String) {
        this.requirements = requirements
    }

    /**
     * Add an unlock to the ending.
     */
    fun unlock(key: String, value: String) {
        unlocks[key] = value
    }

    /**
     * Add metadata to the ending.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Add a tag to the ending.
     */
    fun tag(tag: String) {
        tags.add(tag)
    }

    /**
     * Build the ending.
     */
    fun build(): Ending {
        if (!::id.isInitialized) {
            id = UUID.randomUUID().toString()
        }

        if (!::beatId.isInitialized) {
            throw IllegalStateException("Beat ID must be set")
        }

        return Ending(
            id = id,
            beatId = beatId,
            title = title,
            description = description,
            category = category,
            rarity = rarity,
            requirements = requirements,
            unlocks = unlocks,
            metadata = metadata,
            tags = tags
        )
    }
}

/**
 * Helper class for creating localized text.
 * This is a simplified version of the LocalizedTextDSL class from StoryDSL.kt.
 */
class LocalizedTextDSL {
    private val texts = mutableMapOf<io.github.rmuhamedgaliev.arcana.domain.model.Language, String>()

    /**
     * Set the text for English.
     */
    var en: String
        get() = texts[io.github.rmuhamedgaliev.arcana.domain.model.Language.EN] ?: ""
        set(value) {
            texts[io.github.rmuhamedgaliev.arcana.domain.model.Language.EN] = value
        }

    /**
     * Set the text for Russian.
     */
    var ru: String
        get() = texts[io.github.rmuhamedgaliev.arcana.domain.model.Language.RU] ?: ""
        set(value) {
            texts[io.github.rmuhamedgaliev.arcana.domain.model.Language.RU] = value
        }

    /**
     * Apply the localized text to a LocalizedText object.
     */
    fun applyTo(localizedText: LocalizedText) {
        texts.forEach { (language, text) ->
            localizedText.setText(language, text)
        }
    }
}

/**
 * Create an ending using the builder.
 */
fun ending(id: String, init: EndingBuilder.() -> Unit): Ending {
    val builder = EndingBuilder()
    builder.setId(id)
    builder.init()
    return builder.build()
}
