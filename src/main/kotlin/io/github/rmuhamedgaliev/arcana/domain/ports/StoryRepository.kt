package io.github.rmuhamedgaliev.arcana.domain.ports

import io.github.rmuhamedgaliev.arcana.domain.model.story.Story

/**
 * Repository for managing stories.
 */
interface StoryRepository {
    /**
     * Find a story by its ID.
     *
     * @param id The story ID
     * @return The story, or null if not found
     */
    suspend fun findById(id: String): Story?
    
    /**
     * Find all stories.
     *
     * @return A list of all stories
     */
    suspend fun findAll(): List<Story>
    
    /**
     * Find stories by tag.
     *
     * @param tag The tag to search for
     * @return A list of stories with the specified tag
     */
    suspend fun findByTag(tag: String): List<Story>
    
    /**
     * Save a story.
     *
     * @param story The story to save
     * @return The saved story
     */
    suspend fun save(story: Story): Story
    
    /**
     * Delete a story.
     *
     * @param id The ID of the story to delete
     */
    suspend fun delete(id: String)
    
    /**
     * Load all stories from a directory.
     *
     * @param directoryPath The path to the directory
     * @return A list of loaded stories
     */
    suspend fun loadFromDirectory(directoryPath: String): List<Story>
}
