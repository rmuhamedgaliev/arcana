package io.github.rmuhamedgaliev.arcana.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a complete game with scenes.
 */
public class Game {
    private final String id;
    private final LocalizedText title;
    private final LocalizedText description;
    private final Map<String, GameScene> scenes;
    private final String startSceneId;
    private final Map<String, String> gameAttributes;

    /**
     * Create a new game.
     *
     * @param id           The game ID
     * @param title        The localized title of the game
     * @param description  The localized description of the game
     * @param startSceneId The ID of the starting scene
     */
    public Game(String id, LocalizedText title, LocalizedText description, String startSceneId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scenes = new HashMap<>();
        this.startSceneId = startSceneId;
        this.gameAttributes = new HashMap<>();
    }

    /**
     * Get the game ID.
     *
     * @return The game ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the localized title of the game.
     *
     * @return The localized title
     */
    public LocalizedText getTitle() {
        return title;
    }

    /**
     * Get the localized description of the game.
     *
     * @return The localized description
     */
    public LocalizedText getDescription() {
        return description;
    }

    /**
     * Get the ID of the starting scene.
     *
     * @return The starting scene ID
     */
    public String getStartSceneId() {
        return startSceneId;
    }

    /**
     * Add a scene to the game.
     *
     * @param scene The scene to add
     */
    public void addScene(GameScene scene) {
        scenes.put(scene.getId(), scene);
    }

    /**
     * Get a scene by ID.
     *
     * @param sceneId The scene ID
     * @return The scene, or null if not found
     */
    public GameScene getScene(String sceneId) {
        return scenes.get(sceneId);
    }

    /**
     * Get the starting scene.
     *
     * @return The starting scene
     */
    public GameScene getStartScene() {
        return getScene(startSceneId);
    }

    /**
     * Get all scenes in the game.
     *
     * @return A map of all scenes, keyed by scene ID
     */
    public Map<String, GameScene> getAllScenes() {
        return new HashMap<>(scenes);
    }

    /**
     * Get a game attribute.
     *
     * @param key The attribute key
     * @return The attribute value, or null if not set
     */
    public String getGameAttribute(String key) {
        return gameAttributes.get(key);
    }

    /**
     * Set a game attribute.
     *
     * @param key   The attribute key
     * @param value The attribute value
     */
    public void setGameAttribute(String key, String value) {
        gameAttributes.put(key, value);
    }

    /**
     * Get all game attributes.
     *
     * @return A map of all game attributes
     */
    public Map<String, String> getAllGameAttributes() {
        return new HashMap<>(gameAttributes);
    }
}
