package io.github.rmuhamedgaliev.arcana.core;

import io.github.rmuhamedgaliev.arcana.core.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the current state of the game for a player.
 */
public class GameContext {
    private final Player player;
    private Game currentGame;
    private GameScene currentScene;
    private Language currentLanguage;
    private final Map<String, Game> availableGames;

    /**
     * Create a new game context for a player.
     *
     * @param player The player
     */
    public GameContext(Player player) {
        this.player = player;
        this.currentLanguage = Language.EN;
        this.availableGames = new HashMap<>();
    }

    /**
     * Get the player.
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the current game.
     *
     * @return The current game
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Set the current game.
     *
     * @param game The game to set as current
     */
    public void setCurrentGame(Game game) {
        this.currentGame = game;
        // Reset the current scene to the start scene of the new game
        if (game != null) {
            this.currentScene = game.getStartScene();
        } else {
            this.currentScene = null;
        }
    }

    /**
     * Get the current scene.
     *
     * @return The current scene
     */
    public GameScene getCurrentScene() {
        return currentScene;
    }

    /**
     * Set the current scene.
     *
     * @param scene The scene to set as current
     */
    public void setCurrentScene(GameScene scene) {
        this.currentScene = scene;
    }

    /**
     * Get the current language.
     *
     * @return The current language
     */
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Set the current language.
     *
     * @param language The language to set as current
     */
    public void setCurrentLanguage(Language language) {
        this.currentLanguage = language;
    }

    /**
     * Add a game to the available games.
     *
     * @param game The game to add
     */
    public void addGame(Game game) {
        availableGames.put(game.getId(), game);
    }

    /**
     * Get a game by ID.
     *
     * @param gameId The game ID
     * @return The game, or null if not found
     */
    public Game getGame(String gameId) {
        return availableGames.get(gameId);
    }

    /**
     * Get all available games.
     *
     * @return A map of all available games, keyed by game ID
     */
    public Map<String, Game> getAvailableGames() {
        return new HashMap<>(availableGames);
    }

    /**
     * Save the current game progress for the player.
     */
    public void saveProgress() {
        if (currentGame != null) {
            player.setProgress("currentGameId", currentGame.getId());
            if (currentScene != null) {
                player.setProgress("currentSceneId", currentScene.getId());
            }
        }
    }

    /**
     * Load the saved game progress for the player.
     *
     * @return True if progress was loaded, false otherwise
     */
    public boolean loadProgress() {
        String gameId = (String) player.getProgress("currentGameId");
        if (gameId != null) {
            Game game = getGame(gameId);
            if (game != null) {
                setCurrentGame(game);
                String sceneId = (String) player.getProgress("currentSceneId");
                if (sceneId != null) {
                    GameScene scene = game.getScene(sceneId);
                    if (scene != null) {
                        setCurrentScene(scene);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
