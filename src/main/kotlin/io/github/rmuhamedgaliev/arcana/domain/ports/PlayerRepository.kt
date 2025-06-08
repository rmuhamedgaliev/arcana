package io.github.rmuhamedgaliev.arcana.domain.ports

import io.github.rmuhamedgaliev.arcana.domain.model.player.Player

/**
 * Repository for managing players.
 */
interface PlayerRepository {
    /**
     * Find a player by their ID.
     *
     * @param id The player ID
     * @return The player, or null if not found
     */
    suspend fun findById(id: String): Player?
    
    /**
     * Save a player.
     *
     * @param player The player to save
     * @return The saved player
     */
    suspend fun save(player: Player): Player
    
    /**
     * Delete a player.
     *
     * @param id The ID of the player to delete
     */
    suspend fun delete(id: String)
    
    /**
     * Reset a player's progress.
     *
     * @param id The ID of the player to reset
     */
    suspend fun resetProgress(id: String)
    
    /**
     * Find players by subscription tier.
     *
     * @param subscriptionTier The subscription tier to search for
     * @return A list of players with the specified subscription tier
     */
    suspend fun findBySubscriptionTier(subscriptionTier: String): List<Player>
    
    /**
     * Find players with active subscriptions.
     *
     * @return A list of players with active subscriptions
     */
    suspend fun findWithActiveSubscriptions(): List<Player>
    
    /**
     * Find players with expired subscriptions.
     *
     * @return A list of players with expired subscriptions
     */
    suspend fun findWithExpiredSubscriptions(): List<Player>
}
