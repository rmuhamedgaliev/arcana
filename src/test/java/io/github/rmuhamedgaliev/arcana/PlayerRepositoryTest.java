package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.player.PlayerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerRepositoryTest {
    private PlayerRepository repository;
    private static final String TEST_PLAYER_ID = "test_player_123";
    
    @BeforeEach
    public void setUp() {
        repository = new PlayerRepository();
        repository.initDatabase();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        repository.deletePlayer(TEST_PLAYER_ID);
    }
    
    @Test
    public void testSaveAndLoadPlayer() {
        // Create a test player
        Player player = new Player(TEST_PLAYER_ID);
        player.setAttribute("health", 100);
        player.setAttribute("gold", 50);
        player.setProgress("currentGameId", "adventure");
        player.setProgress("currentSceneId", "village");
        
        // Save the player
        repository.savePlayer(player);
        
        // Load the player
        Optional<Player> loadedPlayerOpt = repository.loadPlayer(TEST_PLAYER_ID);
        
        // Verify the player was loaded
        assertTrue(loadedPlayerOpt.isPresent(), "Player should be found in the database");
        
        Player loadedPlayer = loadedPlayerOpt.get();
        assertEquals(TEST_PLAYER_ID, loadedPlayer.getId(), "Player ID should match");
        assertEquals(100, loadedPlayer.getAttribute("health"), "Health attribute should match");
        assertEquals(50, loadedPlayer.getAttribute("gold"), "Gold attribute should match");
        assertEquals("adventure", loadedPlayer.getProgress("currentGameId"), "Current game ID should match");
        assertEquals("village", loadedPlayer.getProgress("currentSceneId"), "Current scene ID should match");
    }
    
    @Test
    public void testResetPlayerProgress() {
        // Create a test player
        Player player = new Player(TEST_PLAYER_ID);
        player.setAttribute("health", 100);
        player.setAttribute("gold", 50);
        player.setProgress("currentGameId", "adventure");
        player.setProgress("currentSceneId", "village");
        
        // Save the player
        repository.savePlayer(player);
        
        // Reset the player's progress
        repository.resetPlayerProgress(TEST_PLAYER_ID);
        
        // Load the player
        Optional<Player> loadedPlayerOpt = repository.loadPlayer(TEST_PLAYER_ID);
        
        // Verify the player was loaded but progress was reset
        assertTrue(loadedPlayerOpt.isPresent(), "Player should still exist in the database");
        
        Player loadedPlayer = loadedPlayerOpt.get();
        assertEquals(TEST_PLAYER_ID, loadedPlayer.getId(), "Player ID should match");
        assertEquals(0, loadedPlayer.getAttribute("health"), "Health attribute should be reset to 0");
        assertEquals(0, loadedPlayer.getAttribute("gold"), "Gold attribute should be reset to 0");
        assertNull(loadedPlayer.getProgress("currentGameId"), "Current game ID should be null");
        assertNull(loadedPlayer.getProgress("currentSceneId"), "Current scene ID should be null");
    }
    
    @Test
    public void testDeletePlayer() {
        // Create a test player
        Player player = new Player(TEST_PLAYER_ID);
        
        // Save the player
        repository.savePlayer(player);
        
        // Delete the player
        repository.deletePlayer(TEST_PLAYER_ID);
        
        // Try to load the player
        Optional<Player> loadedPlayerOpt = repository.loadPlayer(TEST_PLAYER_ID);
        
        // Verify the player was deleted
        assertFalse(loadedPlayerOpt.isPresent(), "Player should not be found after deletion");
    }
    
    @Test
    public void testLoadNonExistentPlayer() {
        // Try to load a player that doesn't exist
        Optional<Player> loadedPlayerOpt = repository.loadPlayer("non_existent_player");
        
        // Verify the player was not found
        assertFalse(loadedPlayerOpt.isPresent(), "Non-existent player should not be found");
    }
}
