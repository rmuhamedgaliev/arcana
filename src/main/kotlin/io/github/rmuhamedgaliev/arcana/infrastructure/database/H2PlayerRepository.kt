package io.github.rmuhamedgaliev.arcana.infrastructure.database

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.ports.PlayerRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

/**
 * Implementation of PlayerRepository using H2 database.
 */
class H2PlayerRepository(private val databaseConfig: H2DatabaseConfig) : PlayerRepository {
    
    /**
     * Find a player by their ID.
     *
     * @param id The player ID
     * @return The player, or null if not found
     */
    override suspend fun findById(id: String): Player? {
        var connection: Connection? = null
        var playerStmt: PreparedStatement? = null
        var attributesStmt: PreparedStatement? = null
        var progressStmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            
            // Query player
            playerStmt = connection.prepareStatement(
                "SELECT id, created_at, subscription_tier, subscription_expires_at FROM players WHERE id = ?"
            )
            playerStmt.setString(1, id)
            
            val playerRs = playerStmt.executeQuery()
            if (!playerRs.next()) {
                return null
            }
            
            val player = createPlayerFromResultSet(playerRs)
            
            // Query attributes
            attributesStmt = connection.prepareStatement(
                "SELECT attribute_key, attribute_value FROM player_attributes WHERE player_id = ?"
            )
            attributesStmt.setString(1, id)
            
            val attributesRs = attributesStmt.executeQuery()
            while (attributesRs.next()) {
                val key = attributesRs.getString("attribute_key")
                val value = attributesRs.getInt("attribute_value")
                player.attributes[key] = value
            }
            
            // Query progress
            progressStmt = connection.prepareStatement(
                "SELECT progress_key, progress_value FROM player_progress WHERE player_id = ?"
            )
            progressStmt.setString(1, id)
            
            val progressRs = progressStmt.executeQuery()
            while (progressRs.next()) {
                val key = progressRs.getString("progress_key")
                val value = progressRs.getString("progress_value")
                player.progress[key] = value
            }
            
            return player
        } catch (e: SQLException) {
            System.err.println("Error finding player by ID: " + e.message)
            throw RuntimeException("Error finding player by ID", e)
        } finally {
            closeResources(connection, playerStmt, attributesStmt, progressStmt)
        }
    }
    
    /**
     * Save a player.
     *
     * @param player The player to save
     * @return The saved player
     */
    override suspend fun save(player: Player): Player {
        var connection: Connection? = null
        var playerStmt: PreparedStatement? = null
        var deleteAttributesStmt: PreparedStatement? = null
        var insertAttributeStmt: PreparedStatement? = null
        var deleteProgressStmt: PreparedStatement? = null
        var insertProgressStmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            connection.autoCommit = false
            
            // Insert or update player
            playerStmt = connection.prepareStatement(
                "MERGE INTO players (id, created_at, subscription_tier, subscription_expires_at) " +
                "VALUES (?, ?, ?, ?)"
            )
            playerStmt.setString(1, player.id)
            playerStmt.setTimestamp(2, Timestamp.from(player.createdAt))
            playerStmt.setString(3, player.subscriptionTier.name)
            playerStmt.setTimestamp(4, player.subscriptionExpiresAt?.let { Timestamp.from(it) })
            playerStmt.executeUpdate()
            
            // Delete existing attributes
            deleteAttributesStmt = connection.prepareStatement(
                "DELETE FROM player_attributes WHERE player_id = ?"
            )
            deleteAttributesStmt.setString(1, player.id)
            deleteAttributesStmt.executeUpdate()
            
            // Insert attributes
            insertAttributeStmt = connection.prepareStatement(
                "INSERT INTO player_attributes (player_id, attribute_key, attribute_value) VALUES (?, ?, ?)"
            )
            
            for ((key, value) in player.attributes) {
                insertAttributeStmt.setString(1, player.id)
                insertAttributeStmt.setString(2, key)
                insertAttributeStmt.setInt(3, value)
                insertAttributeStmt.executeUpdate()
            }
            
            // Delete existing progress
            deleteProgressStmt = connection.prepareStatement(
                "DELETE FROM player_progress WHERE player_id = ?"
            )
            deleteProgressStmt.setString(1, player.id)
            deleteProgressStmt.executeUpdate()
            
            // Insert progress
            insertProgressStmt = connection.prepareStatement(
                "INSERT INTO player_progress (player_id, progress_key, progress_value) VALUES (?, ?, ?)"
            )
            
            for ((key, value) in player.progress) {
                insertProgressStmt.setString(1, player.id)
                insertProgressStmt.setString(2, key)
                insertProgressStmt.setString(3, value)
                insertProgressStmt.executeUpdate()
            }
            
            connection.commit()
            return player
        } catch (e: SQLException) {
            if (connection != null) {
                try {
                    connection.rollback()
                } catch (rollbackEx: SQLException) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.message)
                }
            }
            
            System.err.println("Error saving player: " + e.message)
            throw RuntimeException("Error saving player", e)
        } finally {
            closeResources(connection, playerStmt, deleteAttributesStmt, insertAttributeStmt, deleteProgressStmt, insertProgressStmt)
        }
    }
    
    /**
     * Delete a player.
     *
     * @param id The ID of the player to delete
     */
    override suspend fun delete(id: String) {
        var connection: Connection? = null
        var stmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            
            // Delete player (cascades to attributes and progress)
            stmt = connection.prepareStatement("DELETE FROM players WHERE id = ?")
            stmt.setString(1, id)
            stmt.executeUpdate()
        } catch (e: SQLException) {
            System.err.println("Error deleting player: " + e.message)
            throw RuntimeException("Error deleting player", e)
        } finally {
            closeResources(connection, stmt)
        }
    }
    
    /**
     * Reset a player's progress.
     *
     * @param id The ID of the player to reset
     */
    override suspend fun resetProgress(id: String) {
        var connection: Connection? = null
        var attributesStmt: PreparedStatement? = null
        var progressStmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            connection.autoCommit = false
            
            // Delete attributes
            attributesStmt = connection.prepareStatement("DELETE FROM player_attributes WHERE player_id = ?")
            attributesStmt.setString(1, id)
            attributesStmt.executeUpdate()
            
            // Delete progress
            progressStmt = connection.prepareStatement("DELETE FROM player_progress WHERE player_id = ?")
            progressStmt.setString(1, id)
            progressStmt.executeUpdate()
            
            connection.commit()
        } catch (e: SQLException) {
            if (connection != null) {
                try {
                    connection.rollback()
                } catch (rollbackEx: SQLException) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.message)
                }
            }
            
            System.err.println("Error resetting player progress: " + e.message)
            throw RuntimeException("Error resetting player progress", e)
        } finally {
            closeResources(connection, attributesStmt, progressStmt)
        }
    }
    
    /**
     * Find players by subscription tier.
     *
     * @param subscriptionTier The subscription tier to search for
     * @return A list of players with the specified subscription tier
     */
    override suspend fun findBySubscriptionTier(subscriptionTier: String): List<Player> {
        var connection: Connection? = null
        var stmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            
            stmt = connection.prepareStatement(
                "SELECT id, created_at, subscription_tier, subscription_expires_at FROM players " +
                "WHERE subscription_tier = ?"
            )
            stmt.setString(1, subscriptionTier)
            
            val rs = stmt.executeQuery()
            val players = ArrayList<Player>()
            
            while (rs.next()) {
                val player = createPlayerFromResultSet(rs)
                players.add(player)
            }
            
            return players
        } catch (e: SQLException) {
            System.err.println("Error finding players by subscription tier: " + e.message)
            throw RuntimeException("Error finding players by subscription tier", e)
        } finally {
            closeResources(connection, stmt)
        }
    }
    
    /**
     * Find players with active subscriptions.
     *
     * @return A list of players with active subscriptions
     */
    override suspend fun findWithActiveSubscriptions(): List<Player> {
        var connection: Connection? = null
        var stmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            
            stmt = connection.prepareStatement(
                "SELECT id, created_at, subscription_tier, subscription_expires_at FROM players " +
                "WHERE subscription_tier != 'FREE' AND " +
                "(subscription_expires_at IS NULL OR subscription_expires_at > CURRENT_TIMESTAMP)"
            )
            
            val rs = stmt.executeQuery()
            val players = ArrayList<Player>()
            
            while (rs.next()) {
                val player = createPlayerFromResultSet(rs)
                players.add(player)
            }
            
            return players
        } catch (e: SQLException) {
            System.err.println("Error finding players with active subscriptions: " + e.message)
            throw RuntimeException("Error finding players with active subscriptions", e)
        } finally {
            closeResources(connection, stmt)
        }
    }
    
    /**
     * Find players with expired subscriptions.
     *
     * @return A list of players with expired subscriptions
     */
    override suspend fun findWithExpiredSubscriptions(): List<Player> {
        var connection: Connection? = null
        var stmt: PreparedStatement? = null
        
        try {
            connection = databaseConfig.getConnection()
            
            stmt = connection.prepareStatement(
                "SELECT id, created_at, subscription_tier, subscription_expires_at FROM players " +
                "WHERE subscription_tier != 'FREE' AND " +
                "subscription_expires_at IS NOT NULL AND subscription_expires_at <= CURRENT_TIMESTAMP"
            )
            
            val rs = stmt.executeQuery()
            val players = ArrayList<Player>()
            
            while (rs.next()) {
                val player = createPlayerFromResultSet(rs)
                players.add(player)
            }
            
            return players
        } catch (e: SQLException) {
            System.err.println("Error finding players with expired subscriptions: " + e.message)
            throw RuntimeException("Error finding players with expired subscriptions", e)
        } finally {
            closeResources(connection, stmt)
        }
    }
    
    /**
     * Create a player from a result set.
     *
     * @param rs The result set
     * @return The player
     */
    private fun createPlayerFromResultSet(rs: ResultSet): Player {
        val id = rs.getString("id")
        val createdAt = rs.getTimestamp("created_at").toInstant()
        val subscriptionTierStr = rs.getString("subscription_tier")
        val subscriptionTier = SubscriptionTier.valueOf(subscriptionTierStr)
        val subscriptionExpiresAt = rs.getTimestamp("subscription_expires_at")?.toInstant()
        
        return Player(
            id = id,
            createdAt = createdAt,
            subscriptionTier = subscriptionTier,
            subscriptionExpiresAt = subscriptionExpiresAt
        )
    }
    
    /**
     * Close database resources.
     *
     * @param connection The connection
     * @param statements The statements
     */
    private fun closeResources(connection: Connection?, vararg statements: PreparedStatement?) {
        for (stmt in statements) {
            if (stmt != null) {
                try {
                    stmt.close()
                } catch (e: SQLException) {
                    // Ignore
                }
            }
        }
        
        if (connection != null) {
            try {
                connection.close()
            } catch (e: SQLException) {
                // Ignore
            }
        }
    }
}
