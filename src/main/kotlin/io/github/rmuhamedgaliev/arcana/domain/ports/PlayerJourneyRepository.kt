package io.github.rmuhamedgaliev.arcana.domain.ports

import io.github.rmuhamedgaliev.arcana.domain.model.player.PlayerJourney

/**
 * Repository for managing player journeys.
 */
interface PlayerJourneyRepository {
    /**
     * Find a player journey by its ID.
     *
     * @param id The journey ID
     * @return The journey, or null if not found
     */
    suspend fun findById(id: String): PlayerJourney?

    /**
     * Find player journeys by player ID.
     *
     * @param playerId The player ID
     * @return A list of journeys for the specified player
     */
    suspend fun findByPlayerId(playerId: String): List<PlayerJourney>

    /**
     * Find player journeys by story ID.
     *
     * @param storyId The story ID
     * @return A list of journeys for the specified story
     */
    suspend fun findByStoryId(storyId: String): List<PlayerJourney>

    /**
     * Find the active journey for a player.
     *
     * @param playerId The player ID
     * @return The active journey, or null if not found
     */
    suspend fun findActiveJourney(playerId: String): PlayerJourney?

    /**
     * Find completed journeys for a player.
     *
     * @param playerId The player ID
     * @return A list of completed journeys for the specified player
     */
    suspend fun findCompletedJourneys(playerId: String): List<PlayerJourney>

    /**
     * Save a player journey.
     *
     * @param journey The journey to save
     * @return The saved journey
     */
    suspend fun save(journey: PlayerJourney): PlayerJourney

    /**
     * Delete a player journey.
     *
     * @param id The ID of the journey to delete
     */
    suspend fun delete(id: String)

    /**
     * Find journeys that visited a specific beat.
     *
     * @param beatId The beat ID
     * @return A list of journeys that visited the specified beat
     */
    suspend fun findByVisitedBeat(beatId: String): List<PlayerJourney>

    /**
     * Find journeys that made a specific choice.
     *
     * @param choiceId The choice ID
     * @return A list of journeys that made the specified choice
     */
    suspend fun findByChoice(choiceId: String): List<PlayerJourney>
}
