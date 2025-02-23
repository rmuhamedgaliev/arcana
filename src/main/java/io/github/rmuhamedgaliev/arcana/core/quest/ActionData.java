package io.github.rmuhamedgaliev.arcana.core.quest;

import java.util.Map;

public class ActionData {
    private String description;
    private String command;
    private Map<String, Object> effect;

    // Геттеры и сеттеры
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getEffect() {
        return effect;
    }

    public void setEffect(Map<String, Object> effect) {
        this.effect = effect;
    }
}
