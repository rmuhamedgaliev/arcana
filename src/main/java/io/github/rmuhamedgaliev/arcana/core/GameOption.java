package io.github.rmuhamedgaliev.arcana.core;

/**
 * Class representing an option in the game.
 * An option is a choice that a player can make in a scene.
 */
public class GameOption {
    private final LocalizedText text;
    private final String nextSceneId;
    private final String condition;

    /**
     * Create a new game option.
     *
     * @param text        The localized text for the option
     * @param nextSceneId The ID of the scene to go to when this option is selected
     */
    public GameOption(LocalizedText text, String nextSceneId) {
        this(text, nextSceneId, null);
    }

    /**
     * Create a new game option with a condition.
     *
     * @param text        The localized text for the option
     * @param nextSceneId The ID of the scene to go to when this option is selected
     * @param condition   A condition expression that must be true for this option to be available
     */
    public GameOption(LocalizedText text, String nextSceneId, String condition) {
        this.text = text;
        this.nextSceneId = nextSceneId;
        this.condition = condition;
    }

    /**
     * Get the localized text for the option.
     *
     * @return The localized text
     */
    public LocalizedText getText() {
        return text;
    }

    /**
     * Get the ID of the scene to go to when this option is selected.
     *
     * @return The next scene ID
     */
    public String getNextSceneId() {
        return nextSceneId;
    }

    /**
     * Get the condition expression for this option.
     *
     * @return The condition expression, or null if there is no condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Check if this option has a condition.
     *
     * @return True if this option has a condition, false otherwise
     */
    public boolean hasCondition() {
        return condition != null && !condition.isEmpty();
    }
}
