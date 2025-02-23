package io.github.rmuhamedgaliev.arcana.core.quest;

import io.github.rmuhamedgaliev.arcana.core.Action;
import io.github.rmuhamedgaliev.arcana.core.GameContext;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private String id;
    private String description;
    private List<Action> actions;

    public Quest(String id, String description) {
        this.id = id;
        this.description = description;
        this.actions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void execute(GameContext context) {
        context.setCurrentQuest(this);
    }
}
