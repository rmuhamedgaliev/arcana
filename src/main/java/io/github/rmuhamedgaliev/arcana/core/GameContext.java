package io.github.rmuhamedgaliev.arcana.core;

import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.quest.Quest;

public class GameContext {
    private Player player;
    private Quest currentQuest;

    public GameContext(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Quest getCurrentQuest() {
        return currentQuest;
    }

    public void setCurrentQuest(Quest currentQuest) {
        this.currentQuest = currentQuest;
    }
}
