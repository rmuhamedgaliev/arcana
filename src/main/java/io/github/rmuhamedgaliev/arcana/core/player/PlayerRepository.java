package io.github.rmuhamedgaliev.arcana.core.player;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing player data in SQLite database.
 */
public class PlayerRepository {
    private static final String DB_URL = "jdbc:sqlite:players.db";
    private static final String CREATE_PLAYERS_TABLE = 
            "CREATE TABLE IF NOT EXISTS players (" +
            "id TEXT PRIMARY KEY, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
    private static final String CREATE_ATTRIBUTES_TABLE = 
            "CREATE TABLE IF NOT EXISTS player_attributes (" +
            "player_id TEXT, " +
            "key TEXT, " +
            "value INTEGER, " +
            "PRIMARY KEY (player_id, key), " +
            "FOREIGN KEY (player_id) REFERENCES players(id)" +
            ")";
    private static final String CREATE_PROGRESS_TABLE = 
            "CREATE TABLE IF NOT EXISTS player_progress (" +
            "player_id TEXT, " +
            "key TEXT, " +
            "value TEXT, " +
            "PRIMARY KEY (player_id, key), " +
            "FOREIGN KEY (player_id) REFERENCES players(id)" +
            ")";
    
    private static final String INSERT_PLAYER = "INSERT OR IGNORE INTO players (id) VALUES (?)";
    private static final String DELETE_PLAYER = "DELETE FROM players WHERE id = ?";
    private static final String DELETE_PLAYER_ATTRIBUTES = "DELETE FROM player_attributes WHERE player_id = ?";
    private static final String DELETE_PLAYER_PROGRESS = "DELETE FROM player_progress WHERE player_id = ?";
    
    private static final String INSERT_ATTRIBUTE = 
            "INSERT OR REPLACE INTO player_attributes (player_id, key, value) VALUES (?, ?, ?)";
    private static final String SELECT_ATTRIBUTES = 
            "SELECT key, value FROM player_attributes WHERE player_id = ?";
    
    private static final String INSERT_PROGRESS = 
            "INSERT OR REPLACE INTO player_progress (player_id, key, value) VALUES (?, ?, ?)";
    private static final String SELECT_PROGRESS = 
            "SELECT key, value FROM player_progress WHERE player_id = ?";
    
    private static final String SELECT_PLAYER_EXISTS = 
            "SELECT 1 FROM players WHERE id = ?";
    
    /**
     * Initialize the database by creating necessary tables if they don't exist.
     */
    public void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_PLAYERS_TABLE);
            stmt.execute(CREATE_ATTRIBUTES_TABLE);
            stmt.execute(CREATE_PROGRESS_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Save a player to the database.
     *
     * @param player The player to save
     */
    public void savePlayer(Player player) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_PLAYER)) {
                stmt.setString(1, player.getId());
                stmt.executeUpdate();
            }
            
            // Save attributes
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_ATTRIBUTE)) {
                for (Map.Entry<String, Integer> entry : player.getAllAttributes().entrySet()) {
                    stmt.setString(1, player.getId());
                    stmt.setString(2, entry.getKey());
                    stmt.setInt(3, entry.getValue());
                    stmt.executeUpdate();
                }
            }
            
            // Save progress
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_PROGRESS)) {
                for (Map.Entry<String, Object> entry : player.getAllProgress().entrySet()) {
                    if (entry.getValue() != null) {
                        stmt.setString(1, player.getId());
                        stmt.setString(2, entry.getKey());
                        stmt.setString(3, entry.getValue().toString());
                        stmt.executeUpdate();
                    }
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player", e);
        }
    }
    
    /**
     * Load a player from the database.
     *
     * @param playerId The ID of the player to load
     * @return The loaded player, or empty if not found
     */
    public Optional<Player> loadPlayer(String playerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if player exists
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_PLAYER_EXISTS)) {
                stmt.setString(1, playerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                }
            }
            
            Player player = new Player(playerId);
            
            // Load attributes
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_ATTRIBUTES)) {
                stmt.setString(1, playerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("key");
                        int value = rs.getInt("value");
                        player.setAttribute(key, value);
                    }
                }
            }
            
            // Load progress
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_PROGRESS)) {
                stmt.setString(1, playerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("key");
                        String value = rs.getString("value");
                        player.setProgress(key, value);
                    }
                }
            }
            
            return Optional.of(player);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load player", e);
        }
    }
    
    /**
     * Delete a player from the database.
     *
     * @param playerId The ID of the player to delete
     */
    public void deletePlayer(String playerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            // Delete player progress
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER_PROGRESS)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
            
            // Delete player attributes
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER_ATTRIBUTES)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
            
            // Delete player
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete player", e);
        }
    }
    
    /**
     * Reset a player's progress in the database.
     *
     * @param playerId The ID of the player to reset
     */
    public void resetPlayerProgress(String playerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            // Delete player progress
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER_PROGRESS)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
            
            // Delete player attributes
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER_ATTRIBUTES)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset player progress", e);
        }
    }
}
