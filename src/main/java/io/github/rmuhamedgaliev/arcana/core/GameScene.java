package io.github.rmuhamedgaliev.arcana.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a scene in the game.
 * A scene is a part of the game with text and options for the player to choose from.
 */
public class GameScene {
    private final String id;
    private final LocalizedText text;
    private final List<GameOption> options;
    private final boolean isEndScene;
    private final Map<String, String> attributes;

    /**
     * Create a new game scene.
     *
     * @param id        The scene ID
     * @param text      The localized text for the scene
     * @param options   The options for the scene
     * @param isEndScene Whether this is an end scene (no options)
     * @param attributes The attributes to set when this scene is reached
     */
    public GameScene(String id, LocalizedText text, List<GameOption> options, boolean isEndScene, Map<String, String> attributes) {
        this.id = id;
        this.text = text;
        this.options = new ArrayList<>(options);
        this.isEndScene = isEndScene;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    /**
     * Create a new game scene.
     *
     * @param id        The scene ID
     * @param text      The localized text for the scene
     * @param options   The options for the scene
     * @param isEndScene Whether this is an end scene (no options)
     */
    public GameScene(String id, LocalizedText text, List<GameOption> options, boolean isEndScene) {
        this(id, text, options, isEndScene, null);
    }

    /**
     * Create a new game scene.
     *
     * @param id        The scene ID
     * @param text      The localized text for the scene
     * @param options   The options for the scene
     */
    public GameScene(String id, LocalizedText text, List<GameOption> options) {
        this(id, text, options, false, null);
    }

    /**
     * Create a new end game scene (no options).
     *
     * @param id        The scene ID
     * @param text      The localized text for the scene
     */
    public GameScene(String id, LocalizedText text) {
        this(id, text, new ArrayList<>(), true, null);
    }

    /**
     * Get the scene ID.
     *
     * @return The scene ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the localized text for the scene.
     *
     * @return The localized text
     */
    public LocalizedText getText() {
        return text;
    }

    /**
     * Get the options for the scene.
     *
     * @return The options
     */
    public List<GameOption> getOptions() {
        return new ArrayList<>(options);
    }

    /**
     * Check if this is an end scene (no options).
     *
     * @return True if this is an end scene, false otherwise
     */
    public boolean isEndScene() {
        return isEndScene;
    }

    /**
     * Add an option to the scene.
     *
     * @param option The option to add
     */
    public void addOption(GameOption option) {
        options.add(option);
    }

    /**
     * Get the attributes for this scene.
     *
     * @return The attributes
     */
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    /**
     * Set an attribute for this scene.
     *
     * @param key   The attribute key
     * @param value The attribute value
     */
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * Get an attribute for this scene.
     *
     * @param key The attribute key
     * @return The attribute value, or null if not set
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }
}
