package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.Map;

/**
 * Class representing an action in a quest.
 * This is used for loading actions from JSON.
 */
public class ActionData {
    private String text;
    private String nextScene;
    private String condition;

    /**
     * Default constructor for Jackson.
     */
    public ActionData() {
    }

    /**
     * Create a new action data.
     *
     * @param text      The text for the action
     * @param nextScene The ID of the next scene
     * @param condition The condition for the action to be available
     */
    public ActionData(String text, String nextScene, String condition) {
        this.text = text;
        this.nextScene = nextScene;
        this.condition = condition;
    }

    /**
     * Get the text for the action.
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text for the action.
     *
     * @param text The text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the ID of the next scene.
     *
     * @return The next scene ID
     */
    public String getNextScene() {
        return nextScene;
    }

    /**
     * Set the ID of the next scene.
     *
     * @param nextScene The next scene ID to set
     */
    public void setNextScene(String nextScene) {
        this.nextScene = nextScene;
    }

    /**
     * Get the condition for the action to be available.
     *
     * @return The condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Set the condition for the action to be available.
     *
     * @param condition The condition to set
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
