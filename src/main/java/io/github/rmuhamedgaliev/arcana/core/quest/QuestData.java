package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.List;

public class QuestData {
    private String id;
    private String description;
    private List<ActionData> actions;

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ActionData> getActions() {
        return actions;
    }

    public void setActions(List<ActionData> actions) {
        this.actions = actions;
    }
}
