package io.github.rmuhamedgaliev.arcana.core.player;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a player in the game.
 * Stores player attributes and progress information.
 */
public class Player {
    private final String id;
    private final Map<String, Integer> attributes;
    private final Map<String, Object> progress;

    /**
     * Create a new player with a random ID.
     */
    public Player() {
        this(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * Create a new player with the specified ID.
     *
     * @param id The player ID
     */
    public Player(String id) {
        this.id = id;
        this.attributes = new HashMap<>();
        this.progress = new HashMap<>();
    }

    /**
     * Get the player ID.
     *
     * @return The player ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set a player attribute.
     *
     * @param key   The attribute key
     * @param value The attribute value
     */
    public void setAttribute(String key, Integer value) {
        attributes.put(key, value);
    }

    /**
     * Get a player attribute.
     *
     * @param key The attribute key
     * @return The attribute value, or 0 if not set
     */
    public int getAttribute(String key) {
        return attributes.getOrDefault(key, 0);
    }

    /**
     * Check if a player has an attribute.
     *
     * @param key The attribute key
     * @return True if the player has the attribute, false otherwise
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Get all player attributes.
     *
     * @return A map of all player attributes
     */
    public Map<String, Integer> getAllAttributes() {
        return new HashMap<>(attributes);
    }

    /**
     * Set a progress value.
     *
     * @param key   The progress key
     * @param value The progress value
     */
    public void setProgress(String key, Object value) {
        progress.put(key, value);
    }

    /**
     * Get a progress value.
     *
     * @param key The progress key
     * @return The progress value, or null if not set
     */
    public Object getProgress(String key) {
        return progress.get(key);
    }

    /**
     * Check if a player has a progress value.
     *
     * @param key The progress key
     * @return True if the player has the progress value, false otherwise
     */
    public boolean hasProgress(String key) {
        return progress.containsKey(key);
    }

    /**
     * Get all player progress values.
     *
     * @return A map of all player progress values
     */
    public Map<String, Object> getAllProgress() {
        return new HashMap<>(progress);
    }
}
