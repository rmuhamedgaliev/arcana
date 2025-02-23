package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.List;
import java.util.Map;

public class GameData {
    private Map<String, Integer> initialAttributes; // Начальные атрибуты игрока
    private List<QuestData> quests;
    private List<EndCondition> endConditions;

    // Геттеры и сеттеры
    public Map<String, Integer> getInitialAttributes() {
        return initialAttributes;
    }

    public void setInitialAttributes(Map<String, Integer> initialAttributes) {
        this.initialAttributes = initialAttributes;
    }

    public List<QuestData> getQuests() {
        return quests;
    }

    public void setQuests(List<QuestData> quests) {
        this.quests = quests;
    }

    public List<EndCondition> getEndConditions() {
        return endConditions;
    }

    public void setEndConditions(List<EndCondition> endConditions) {
        this.endConditions = endConditions;
    }
}
