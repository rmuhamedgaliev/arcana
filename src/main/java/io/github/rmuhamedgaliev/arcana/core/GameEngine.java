package io.github.rmuhamedgaliev.arcana.core;

import io.github.rmuhamedgaliev.arcana.core.quest.ActionData;
import io.github.rmuhamedgaliev.arcana.core.quest.EndCondition;
import io.github.rmuhamedgaliev.arcana.core.quest.GameData;
import io.github.rmuhamedgaliev.arcana.core.quest.Quest;
import io.github.rmuhamedgaliev.arcana.core.quest.QuestData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEngine {
    private final Map<String, Quest> quests; // Хранит все квесты по их ID
    private final GameContext context; // Контекст игры (игрок, текущий квест и т.д.)
    private final GameInterface gameInterface; // Интерфейс для взаимодействия с игроком
    private boolean isRunning; // Флаг, указывающий, работает ли игра
    private List<EndCondition> endConditions; // Условия завершения игры

    public GameEngine(GameContext context, GameInterface gameInterface) {
        this.context = context;
        this.gameInterface = gameInterface;
        this.quests = new HashMap<>();
        this.isRunning = true;
    }

    /**
     * Загружает квесты и условия завершения из GameData.
     *
     * @param gameData Данные игры (квесты и условия завершения).
     */
    public void loadQuests(GameData gameData) {
        this.endConditions = gameData.getEndConditions(); // Загружаем условия завершения
        for (QuestData questData : gameData.getQuests()) {
            Quest quest = new Quest(questData.getId(), questData.getDescription());
            for (ActionData actionData : questData.getActions()) {
                Action action = new Action(
                        actionData.getDescription(),
                        actionData.getCommand(),
                        ctx -> {
                            boolean attributesChanged = false; // Флаг изменения атрибутов
                            // Применяем эффекты
                            Map<String, Object> effect = actionData.getEffect();
                            if (effect.containsKey("setAttribute")) {
                                Map<String, Integer> attributes = (Map<String, Integer>) effect.get("setAttribute");
                                attributes.forEach((key, value) -> {
                                    int currentValue = ctx.getPlayer().getAttributeAsInt(key, 0);
                                    ctx.getPlayer().setAttribute(key, currentValue + value);
                                });
                                attributesChanged = true; // Атрибуты изменились
                            }
                            showStatusIfChanged(attributesChanged); // Показываем статусы, если атрибуты изменились
                            // Переходим к следующему квесту
                            if (effect.containsKey("nextQuest")) {
                                executeQuest((String) effect.get("nextQuest"));
                            }
                            // Завершаем игру, если указано
                            if (effect.containsKey("endGame") && (boolean) effect.get("endGame")) {
                                endGame();
                            }
                        }
                );
                quest.addAction(action);
            }
            quests.put(quest.getId(), quest);
        }
    }

    /**
     * Запускает игру.
     */
    public void start() {
        gameInterface.sendMessage("Добро пожаловать в текстовый квест!");
        executeQuest("start"); // Начинаем с первого квеста

        while (isRunning) {
            Quest currentQuest = context.getCurrentQuest();
            if (currentQuest == null) {
                gameInterface.sendMessage("Игра завершена.");
                break;
            }
            checkEndConditions(); // Проверяем условия завершения
            if (!isRunning) break; // Если игра завершена, выходим из цикла
            showActions(currentQuest); // Показываем доступные действия
        }
    }

    /**
     * Проверяет условия завершения игры.
     */
    private void checkEndConditions() {
        for (EndCondition condition : endConditions) {
            if (condition.getType().equals("attribute")) {
                // Проверяем атрибуты игрока
                int currentValue = context.getPlayer().getAttributeAsInt(condition.getAttribute(), 0);
                if (currentValue <= condition.getValue()) {
                    gameInterface.sendMessage(condition.getMessage());
                    endGame();
                    return;
                }
            } else if (condition.getType().equals("step")) {
                // Проверяем текущий шаг
                if (context.getCurrentQuest().getId().equals(condition.getStep())) {
                    gameInterface.sendMessage(condition.getMessage());
                    endGame();
                    return;
                }
            }
        }
    }

    /**
     * Выполняет квест по его ID.
     *
     * @param questId ID квеста.
     */
    public void executeQuest(String questId) {
        Quest quest = quests.get(questId);
        if (quest != null) {
            quest.execute(context); // Устанавливаем текущий квест
        } else {
            gameInterface.sendMessage("Квест не найден!");
        }
    }

    /**
     * Показывает доступные действия для текущего квеста.
     *
     * @param quest Текущий квест.
     */
    private void showActions(Quest quest) {
        gameInterface.sendMessage(quest.getDescription()); // Описание квеста
        List<Action> actions = quest.getActions();
        for (int i = 0; i < actions.size(); i++) {
            gameInterface.sendMessage((i + 1) + ". " + actions.get(i).getDescription()); // Список действий
        }
        handleInput(actions); // Обрабатываем ввод игрока
    }

    /**
     * Обрабатывает ввод игрока.
     *
     * @param actions Список доступных действий.
     */
    private void handleInput(List<Action> actions) {
        String input = gameInterface.receiveInput(); // Получаем ввод от игрока
        try {
            int choice = Integer.parseInt(input) - 1; // Преобразуем ввод в число
            if (choice >= 0 && choice < actions.size()) {
                actions.get(choice).execute(context); // Выполняем выбранное действие
            } else {
                gameInterface.sendMessage("Неверный выбор. Попробуйте снова.");
            }
        } catch (NumberFormatException e) {
            gameInterface.sendMessage("Пожалуйста, введите номер действия.");
        }
    }

    /**
     * Показывает текущие статусы игрока, если атрибуты изменились.
     *
     * @param attributesChanged Флаг, указывающий, изменились ли атрибуты.
     */
    private void showStatusIfChanged(boolean attributesChanged) {
        if (attributesChanged) {
            gameInterface.sendMessage("--- Текущие статусы ---");
            context.getPlayer().getAttributes().forEach((key, value) -> {
                gameInterface.sendMessage(key + ": " + value);
            });
            gameInterface.sendMessage("----------------------");
        }
    }

    /**
     * Завершает игру.
     */
    public void endGame() {
        isRunning = false;
        gameInterface.sendMessage("Спасибо за игру!");
    }
}
