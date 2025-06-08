package io.github.rmuhamedgaliev.arcana.infrastructure.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ChoiceWeight
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.domain.ports.StoryRepository
import io.github.rmuhamedgaliev.arcana.infrastructure.config.AppConfig
import mu.KotlinLogging
import java.io.File
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Implementation of StoryRepository that loads stories from JSON files.
 */
class JsonStoryRepository(
    private val config: AppConfig,
    private val objectMapper: ObjectMapper
) : StoryRepository {

    /**
     * Find a story by ID.
     *
     * @param id The story ID
     * @return The story, or null if not found
     */
    override suspend fun findById(id: String): Story? {
        val gameFile = File("${config.gamesDirectory}/$id.json")
        if (!gameFile.exists()) {
            logger.warn { "Story file not found: $gameFile" }
            return null
        }

        return try {
            val jsonData = gameFile.readText()
            val gameData = objectMapper.readValue<GameData>(jsonData)
            convertToStory(gameData)
        } catch (e: Exception) {
            logger.error(e) { "Error loading story from JSON: $id" }
            null
        }
    }

    /**
     * Find all stories.
     *
     * @return A list of all stories
     */
    override suspend fun findAll(): List<Story> {
        val gamesDir = File(config.gamesDirectory)
        if (!gamesDir.exists() || !gamesDir.isDirectory) {
            logger.warn { "Games directory not found: $gamesDir" }
            return emptyList()
        }

        val stories = mutableListOf<Story>()

        gamesDir.listFiles { file -> file.isFile && file.name.endsWith(".json") }?.forEach { file ->
            try {
                val jsonData = file.readText()
                val gameData = objectMapper.readValue<GameData>(jsonData)
                val story = convertToStory(gameData)
                if (story != null) {
                    stories.add(story)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error loading story from JSON: ${file.name}" }
            }
        }

        return stories
    }

    /**
     * Find stories by tag.
     *
     * @param tag The tag to search for
     * @return A list of stories with the specified tag
     */
    override suspend fun findByTag(tag: String): List<Story> {
        return findAll().filter { it.hasTag(tag) }
    }

    /**
     * Save a story.
     *
     * @param story The story to save
     * @return The saved story
     */
    override suspend fun save(story: Story): Story {
        // Not implemented for JSON repository
        logger.warn { "Save operation not implemented for JSON repository" }
        return story
    }

    /**
     * Delete a story.
     *
     * @param id The ID of the story to delete
     */
    override suspend fun delete(id: String) {
        // Not implemented for JSON repository
        logger.warn { "Delete operation not implemented for JSON repository" }
    }

    /**
     * Load all stories from a directory.
     *
     * @param directoryPath The path to the directory
     * @return A list of loaded stories
     */
    override suspend fun loadFromDirectory(directoryPath: String): List<Story> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) {
            logger.warn { "Directory not found: $dir" }
            return emptyList()
        }

        val stories = mutableListOf<Story>()

        dir.listFiles { file -> file.isFile && file.name.endsWith(".json") }?.forEach { file ->
            try {
                val jsonData = file.readText()
                val gameData = objectMapper.readValue<GameData>(jsonData)
                val story = convertToStory(gameData)
                if (story != null) {
                    stories.add(story)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error loading story from JSON: ${file.name}" }
            }
        }

        return stories
    }

    /**
     * Convert GameData to Story.
     *
     * @param gameData The game data
     * @return The story
     */
    private fun convertToStory(gameData: GameData): Story? {
        try {
            // Create localized title and description
            val title = LocalizedText()
            val description = LocalizedText()

            gameData.localizedData.forEach { (languageCode, localizedGameData) ->
                val language = Language.fromCode(languageCode) ?: return@forEach
                title.setText(language, localizedGameData.title)
                description.setText(language, localizedGameData.description)
            }

            // Create story
            val story = Story(
                id = gameData.id,
                title = title,
                description = description,
                startBeatId = gameData.startSceneId,
                requiredSubscriptionTier = SubscriptionTier.FREE // Default to free
            )

            // Add tags
            gameData.tags?.forEach { tag ->
                story.addTag(tag)
            }

            // Add metadata
            gameData.metadata?.forEach { (key, value) ->
                story.addMetadata(key, value)
            }

            // Convert scenes to beats
            gameData.scenes.forEach { (sceneId, sceneData) ->
                val beatText = LocalizedText()

                // Add localized text for each language
                gameData.localizedData.forEach { (languageCode, localizedGameData) ->
                    val language = Language.fromCode(languageCode) ?: return@forEach
                    val localizedScene = localizedGameData.scenes[sceneId]
                    if (localizedScene != null) {
                        beatText.setText(language, localizedScene.text)
                    }
                }

                // Create beat
                val beat = StoryBeat(
                    id = sceneId,
                    text = beatText,
                    isEndBeat = sceneData.isEndScene ?: false
                )

                // Add attributes
                sceneData.attributes?.forEach { (key, value) ->
                    beat.addAttribute(key, value)
                }

                // Add tags
                sceneData.tags?.forEach { tag ->
                    beat.addTag(tag)
                }

                // Add metadata
                sceneData.metadata?.forEach { (key, value) ->
                    beat.addMetadata(key, value)
                }

                // Add choices
                sceneData.options?.forEach { optionData ->
                    val choiceText = LocalizedText()

                    // Add localized text for each language
                    gameData.localizedData.forEach { (languageCode, localizedGameData) ->
                        val language = Language.fromCode(languageCode) ?: return@forEach
                        val localizedScene = localizedGameData.scenes[sceneId]
                        if (localizedScene != null) {
                            val localizedOption = localizedScene.options?.find { 
                                it.nextSceneId == optionData.nextSceneId 
                            }
                            if (localizedOption != null) {
                                choiceText.setText(language, localizedOption.text)
                            }
                        }
                    }

                    // Create choice
                    val choice = NarrativeChoice(
                        id = UUID.randomUUID().toString(), // Generate a unique ID
                        text = choiceText,
                        nextBeatId = optionData.nextSceneId,
                        condition = optionData.condition,
                        weight = ChoiceWeight.NORMAL // Default weight
                    )

                    // Add tags
                    optionData.tags?.forEach { tag ->
                        choice.addTag(tag)
                    }

                    // Add metadata
                    optionData.metadata?.forEach { (key, value) ->
                        choice.addMetadata(key, value)
                    }

                    // Add consequences
                    optionData.consequences?.forEach { consequenceData ->
                        val consequence = Consequence(
                            id = UUID.randomUUID().toString(),
                            type = ConsequenceType.valueOf(consequenceData.type),
                            target = consequenceData.target,
                            value = consequenceData.value,
                            delay = consequenceData.delay ?: 0
                        )
                        choice.addConsequence(consequence)
                    }

                    beat.addChoice(choice)
                }

                story.addBeat(beat)
            }

            return story
        } catch (e: Exception) {
            logger.error(e) { "Error converting GameData to Story" }
            return null
        }
    }

    /**
     * Data class for JSON game data.
     */
    data class GameData(
        val id: String,
        val startSceneId: String,
        val scenes: Map<String, SceneData>,
        val localizedData: Map<String, LocalizedGameData>,
        val tags: List<String>? = null,
        val metadata: Map<String, String>? = null
    )

    /**
     * Data class for JSON scene data.
     */
    data class SceneData(
        val options: List<OptionData>? = null,
        val isEndScene: Boolean? = false,
        val attributes: Map<String, String>? = null,
        val tags: List<String>? = null,
        val metadata: Map<String, String>? = null
    )

    /**
     * Data class for JSON option data.
     */
    data class OptionData(
        val nextSceneId: String,
        val condition: String? = null,
        val consequences: List<ConsequenceData>? = null,
        val tags: List<String>? = null,
        val metadata: Map<String, String>? = null
    )

    /**
     * Data class for JSON consequence data.
     */
    data class ConsequenceData(
        val type: String,
        val target: String,
        val value: String,
        val delay: Int? = 0
    )

    /**
     * Data class for JSON localized game data.
     */
    data class LocalizedGameData(
        val title: String,
        val description: String,
        val scenes: Map<String, LocalizedSceneData>
    )

    /**
     * Data class for JSON localized scene data.
     */
    data class LocalizedSceneData(
        val text: String,
        val options: List<LocalizedOptionData>? = null
    )

    /**
     * Data class for JSON localized option data.
     */
    data class LocalizedOptionData(
        val text: String,
        val nextSceneId: String
    )
}
