package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a complete game with quests and localized data.
 * This is used for loading games from JSON.
 */
public class GameData {
    private String id;
    private String defaultLanguage;
    private Map<String, Integer> initialAttributes;
    private List<QuestData> quests;
    private List<LocalizedGameData> localizations;
    private String startQuestId;

    /**
     * Default constructor for Jackson.
     */
    public GameData() {
        this.initialAttributes = new HashMap<>();
        this.quests = new ArrayList<>();
        this.localizations = new ArrayList<>();
    }

    /**
     * Create a new game data.
     *
     * @param id                The game ID
     * @param defaultLanguage   The default language code
     * @param initialAttributes The initial attributes for the player
     * @param quests            The quests in the game
     * @param localizations     The localized data for the game
     * @param startQuestId      The ID of the starting quest
     */
    public GameData(String id, String defaultLanguage, Map<String, Integer> initialAttributes,
                   List<QuestData> quests, List<LocalizedGameData> localizations, String startQuestId) {
        this.id = id;
        this.defaultLanguage = defaultLanguage;
        this.initialAttributes = initialAttributes;
        this.quests = quests;
        this.localizations = localizations;
        this.startQuestId = startQuestId;
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
     * Set the game ID.
     *
     * @param id The game ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the default language code.
     *
     * @return The default language code
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Set the default language code.
     *
     * @param defaultLanguage The default language code to set
     */
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Get the initial attributes for the player.
     *
     * @return The initial attributes
     */
    public Map<String, Integer> getInitialAttributes() {
        return initialAttributes;
    }

    /**
     * Set the initial attributes for the player.
     *
     * @param initialAttributes The initial attributes to set
     */
    public void setInitialAttributes(Map<String, Integer> initialAttributes) {
        this.initialAttributes = initialAttributes;
    }

    /**
     * Get the quests in the game.
     *
     * @return The quests
     */
    public List<QuestData> getQuests() {
        return quests;
    }

    /**
     * Set the quests in the game.
     *
     * @param quests The quests to set
     */
    public void setQuests(List<QuestData> quests) {
        this.quests = quests;
    }

    /**
     * Get the localized data for the game.
     *
     * @return The localized data
     */
    public List<LocalizedGameData> getLocalizations() {
        return localizations;
    }

    /**
     * Set the localized data for the game.
     *
     * @param localizations The localized data to set
     */
    public void setLocalizations(List<LocalizedGameData> localizations) {
        this.localizations = localizations;
    }

    /**
     * Get the ID of the starting quest.
     *
     * @return The starting quest ID
     */
    public String getStartQuestId() {
        return startQuestId;
    }

    /**
     * Set the ID of the starting quest.
     *
     * @param startQuestId The starting quest ID to set
     */
    public void setStartQuestId(String startQuestId) {
        this.startQuestId = startQuestId;
    }

    /**
     * Get a quest by ID.
     *
     * @param questId The quest ID
     * @return The quest, or null if not found
     */
    public QuestData getQuest(String questId) {
        for (QuestData quest : quests) {
            if (quest.getId().equals(questId)) {
                return quest;
            }
        }
        return null;
    }

    /**
     * Get localized data for a language.
     *
     * @param language The language code
     * @return The localized data, or null if not found
     */
    public LocalizedGameData getLocalization(String language) {
        for (LocalizedGameData localization : localizations) {
            if (localization.getLanguage().equals(language)) {
                return localization;
            }
        }
        // Fallback to default language
        if (!language.equals(defaultLanguage)) {
            return getLocalization(defaultLanguage);
        }
        return null;
    }
}
