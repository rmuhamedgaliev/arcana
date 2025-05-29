package io.github.rmuhamedgaliev.arcana.core.quest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a quest in a game.
 * This is used for loading quests from JSON.
 */
public class QuestData {
    private String id;
    private String text;
    private List<ActionData> actions;
    @JsonProperty("isEnd")
    private boolean end;
    private Map<String, String> attributes;

    /**
     * Default constructor for Jackson.
     */
    public QuestData() {
        this.actions = new ArrayList<>();
        this.attributes = new HashMap<>();
    }

    /**
     * Create a new quest data.
     *
     * @param id        The quest ID
     * @param text      The text for the quest
     * @param actions   The actions for the quest
     * @param isEnd     Whether this is an end quest
     * @param attributes The attributes to set when this quest is reached
     */
    public QuestData(String id, String text, List<ActionData> actions, boolean isEnd, Map<String, String> attributes) {
        this.id = id;
        this.text = text;
        this.actions = actions;
        this.end = isEnd;
        this.attributes = attributes;
    }

    /**
     * Get the quest ID.
     *
     * @return The quest ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set the quest ID.
     *
     * @param id The quest ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the text for the quest.
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text for the quest.
     *
     * @param text The text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get the actions for the quest.
     *
     * @return The actions
     */
    public List<ActionData> getActions() {
        return actions;
    }

    /**
     * Set the actions for the quest.
     *
     * @param actions The actions to set
     */
    public void setActions(List<ActionData> actions) {
        this.actions = actions;
    }

    /**
     * Check if this is an end quest.
     *
     * @return True if this is an end quest, false otherwise
     */
    public boolean isEnd() {
        return end;
    }

    /**
     * Set whether this is an end quest.
     *
     * @param end True if this is an end quest, false otherwise
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * Get the attributes to set when this quest is reached.
     *
     * @return The attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Set the attributes to set when this quest is reached.
     *
     * @param attributes The attributes to set
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
