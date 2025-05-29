package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing localized game data.
 * This is used for loading localized game data from JSON.
 */
public class LocalizedGameData {
    private String language;
    private String title;
    private String description;
    private Map<String, String> questTexts;
    private Map<String, Map<String, String>> actionTexts;

    /**
     * Default constructor for Jackson.
     */
    public LocalizedGameData() {
        this.questTexts = new HashMap<>();
        this.actionTexts = new HashMap<>();
    }

    /**
     * Create new localized game data.
     *
     * @param language    The language code
     * @param title       The localized title
     * @param description The localized description
     * @param questTexts  The localized quest texts
     * @param actionTexts The localized action texts
     */
    public LocalizedGameData(String language, String title, String description,
                            Map<String, String> questTexts, Map<String, Map<String, String>> actionTexts) {
        this.language = language;
        this.title = title;
        this.description = description;
        this.questTexts = questTexts;
        this.actionTexts = actionTexts;
    }

    /**
     * Get the language code.
     *
     * @return The language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set the language code.
     *
     * @param language The language code to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the localized title.
     *
     * @return The localized title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the localized title.
     *
     * @param title The localized title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the localized description.
     *
     * @return The localized description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the localized description.
     *
     * @param description The localized description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the localized quest texts.
     *
     * @return The localized quest texts
     */
    public Map<String, String> getQuestTexts() {
        return questTexts;
    }

    /**
     * Set the localized quest texts.
     *
     * @param questTexts The localized quest texts to set
     */
    public void setQuestTexts(Map<String, String> questTexts) {
        this.questTexts = questTexts;
    }

    /**
     * Get the localized action texts.
     *
     * @return The localized action texts
     */
    public Map<String, Map<String, String>> getActionTexts() {
        return actionTexts;
    }

    /**
     * Set the localized action texts.
     *
     * @param actionTexts The localized action texts to set
     */
    public void setActionTexts(Map<String, Map<String, String>> actionTexts) {
        this.actionTexts = actionTexts;
    }

    /**
     * Get the localized text for a quest.
     *
     * @param questId The quest ID
     * @return The localized text, or null if not found
     */
    public String getQuestText(String questId) {
        return questTexts.get(questId);
    }

    /**
     * Get the localized text for an action.
     *
     * @param questId  The quest ID
     * @param actionId The action ID (index)
     * @return The localized text, or null if not found
     */
    public String getActionText(String questId, String actionId) {
        Map<String, String> questActions = actionTexts.get(questId);
        if (questActions != null) {
            return questActions.get(actionId);
        }
        return null;
    }
}
