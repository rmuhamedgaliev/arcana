package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameContext;
import io.github.rmuhamedgaliev.arcana.core.GameEngine;
import io.github.rmuhamedgaliev.arcana.core.Language;
import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.ports.GameOutputPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TelegramBotTest {

    @Test
    public void testGameEngineLoadsGames() {
        // Create a player, context, and interface
        Player player = new Player("test-user");
        GameContext context = new GameContext(player);
        MockGameInterface gameInterface = new MockGameInterface();

        // Create the game engine
        GameEngine engine = new GameEngine(context, gameInterface);

        // Test that loading games doesn't throw an exception
        assertDoesNotThrow(() -> {
            engine.loadAllGames("games");
            System.out.println("[DEBUG_LOG] Available games: " + context.getAvailableGames().size());
            context.getAvailableGames().forEach((id, game) -> {
                System.out.println("[DEBUG_LOG] Game: " + id + " - " + game.getTitle().getText(gameInterface.getCurrentLanguage()));
            });
        }, "Should be able to load games without exceptions");
    }

    @Test
    public void testGameEngineStartsGame() {
        // Create a player, context, and interface
        Player player = new Player("test-user");
        GameContext context = new GameContext(player);
        MockGameInterface gameInterface = new MockGameInterface();

        // Create the game engine
        GameEngine engine = new GameEngine(context, gameInterface);

        // Test that starting a game doesn't throw an exception
        assertDoesNotThrow(() -> {
            // Load games
            engine.loadAllGames("games");

            // Verify games were loaded
            int gameCount = context.getAvailableGames().size();
            System.out.println("[DEBUG_LOG] Available games: " + gameCount);
            assert gameCount > 0 : "No games were loaded";

            // Create a thread to stop the engine after a short delay
            Thread stopThread = new Thread(() -> {
                try {
                    // Wait a short time to let the game initialize
                    Thread.sleep(500);
                    // Stop the engine
                    engine.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            stopThread.setDaemon(true);
            stopThread.start();

            // Start the game (this will select language, game, and enter the game loop)
            // We're using a mock interface that always selects the first option
            engine.start();

            // Verify that a game was selected
            assert context.getCurrentGame() != null : "No game was selected";
            System.out.println("[DEBUG_LOG] Selected game: " + context.getCurrentGame().getId());

            // Verify that a scene was selected
            assert context.getCurrentScene() != null : "No scene was selected";
            System.out.println("[DEBUG_LOG] Current scene: " + context.getCurrentScene().getId());
        }, "Should be able to start a game without exceptions");
    }

    /**
     * Mock implementation of GameOutputPort for testing.
     */
    private static class MockGameInterface implements GameOutputPort {
        private Language currentLanguage = Language.EN;

        @Override
        public void sendMessage(String message) {
            System.out.println("[DEBUG_LOG] Message: " + message);
        }

        @Override
        public int sendOptionsMessage(String message, java.util.List<String> options) {
            System.out.println("[DEBUG_LOG] Options message: " + message);
            for (int i = 0; i < options.size(); i++) {
                System.out.println("[DEBUG_LOG] Option " + (i + 1) + ": " + options.get(i));
            }
            // Always select the first option for testing
            return 0;
        }

        @Override
        public Language getCurrentLanguage() {
            return currentLanguage;
        }

        @Override
        public void setCurrentLanguage(Language language) {
            this.currentLanguage = language;
        }

        @Override
        public void displayPlayerStatus(java.util.Map<String, Integer> attributes) {
            System.out.println("[DEBUG_LOG] Player status:");
            for (java.util.Map.Entry<String, Integer> entry : attributes.entrySet()) {
                System.out.println("[DEBUG_LOG] " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
