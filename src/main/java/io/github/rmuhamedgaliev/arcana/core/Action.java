package io.github.rmuhamedgaliev.arcana.core;

import java.util.function.Consumer;

public class Action {
    private String description;
    private String command;
    private Consumer<GameContext> effect;

    public Action(String description, String command, Consumer<GameContext> effect) {
        this.description = description;
        this.command = command;
        this.effect = effect;
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }

    public void execute(GameContext context) {
        effect.accept(context);
    }
}
