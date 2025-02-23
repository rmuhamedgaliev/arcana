package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameContext;
import io.github.rmuhamedgaliev.arcana.core.GameEngine;
import io.github.rmuhamedgaliev.arcana.core.quest.GameData;
import io.github.rmuhamedgaliev.arcana.core.quest.GameLoader;
import io.github.rmuhamedgaliev.arcana.core.player.Player;

import java.io.IOException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Player player = new Player();
        GameContext context = new GameContext(player);
        ConsoleInterface consoleInterface = new ConsoleInterface();
        GameEngine engine = new GameEngine(context, consoleInterface);

        try {
            // Загружаем сценарий из JSON
            GameData gameData = GameLoader.loadGameData("scenario.json");

            // Инициализируем атрибуты игрока
            Map<String, Integer> initialAttributes = gameData.getInitialAttributes();
            if (initialAttributes != null) {
                initialAttributes.forEach(player::setAttribute);
            }

            // Загружаем квесты
            engine.loadQuests(gameData);

            // Запускаем игру
            engine.start();
        } catch (IOException e) {
            consoleInterface.sendMessage("Ошибка загрузки сценария: " + e.getMessage());
        }
    }
}
