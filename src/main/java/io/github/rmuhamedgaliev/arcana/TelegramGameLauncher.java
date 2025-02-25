package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameContext;
import io.github.rmuhamedgaliev.arcana.core.GameEngine;
import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.quest.GameData;
import io.github.rmuhamedgaliev.arcana.core.quest.GameLoader;
import io.github.rmuhamedgaliev.arcana.telegram.TelegramInterface;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TelegramGameLauncher {
    private static class GameInstance {
        private final GameEngine engine;
        private final Thread gameThread;

        public GameInstance(GameEngine engine, Thread gameThread) {
            this.engine = engine;
            this.gameThread = gameThread;
        }

        public void stop() {
            engine.endGame();
            gameThread.interrupt();
            try {
                gameThread.join(5000); // Ждем завершения потока максимум 5 секунд
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final String BOT_TOKEN = "7570743326:AAHeDE12W84B3-Bar1rb5El8StZsoKB6Wxk";
    private static final String BOT_USERNAME = "rm_test_bot_playground_bot";
    private static final AtomicReference<GameInstance> currentGame = new AtomicReference<>();
    private static TelegramInterface telegramInterface;

    public static void main(String[] args) {
        try {
            new TelegramGameLauncher().launch();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void launch() throws TelegramApiException {
        telegramInterface = createTelegramInterface();
        registerBot(telegramInterface);
    }

    private TelegramInterface createTelegramInterface() {
        return new TelegramInterface(BOT_TOKEN, BOT_USERNAME, this::startNewGame);
    }

    private void registerBot(TelegramInterface telegramInterface) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramInterface);
    }

    private void startNewGame() {
        // Останавливаем текущую игру, если она существует
        GameInstance oldGame = currentGame.get();
        if (oldGame != null) {
            oldGame.stop();
        }

        Player player = new Player();
        GameContext context = new GameContext(player);
        GameEngine engine = new GameEngine(context, telegramInterface);

        try {
            GameData gameData = GameLoader.loadGameData("scenario.json");
            Map<String, Integer> initialAttributes = gameData.getInitialAttributes();
            if (initialAttributes != null) {
                initialAttributes.forEach(player::setAttribute);
            }
            engine.loadQuests(gameData);

            Thread gameThread = new Thread(() -> {
                try {
                    telegramInterface.setGameStarted(true);
                    engine.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            gameThread.start();
            currentGame.set(new GameInstance(engine, gameThread));
        } catch (IOException e) {
            telegramInterface.sendMessage("Ошибка загрузки сценария: " + e.getMessage());
        }
    }
}