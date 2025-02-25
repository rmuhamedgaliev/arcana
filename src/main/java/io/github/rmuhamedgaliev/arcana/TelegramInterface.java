package io.github.rmuhamedgaliev.arcana.telegram;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelegramInterface extends TelegramLongPollingBot implements GameInterface {
    private final String botToken;
    private final String botUsername;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Long currentChatId;
    private boolean isGameStarted = false;
    private final Runnable restartAction;

    public TelegramInterface(String botToken, String botUsername, Runnable restartAction) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.restartAction = restartAction;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            currentChatId = update.getMessage().getChatId();

            if (messageText.equals("/start") || messageText.equals("/restart")) {
                isGameStarted = false;
                restartAction.run();
                return;
            }

            // Добавляем ввод в очередь только если игра запущена
            if (isGameStarted) {
                inputQueue.offer(messageText);
            } else {
                sendMessage("Используйте /start для начала игры или /restart для перезапуска");
            }
        }
    }

    private void showStartMessage() {
        SendMessage message = new SendMessage();
        message.setChatId(currentChatId.toString());
        message.setText("Добро пожаловать в игру!\n" +
                "Следуйте инструкциям и используйте цифры для выбора действий.\n" +
                "Игра начинается...");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/start");
        row1.add("/restart");
        keyboard.add(row1);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (currentChatId != null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(currentChatId.toString());

            if (message.contains("Конец игры") || message.contains("Game Over")) {
                isGameStarted = false;
                message += "\n\nИспользуйте /start для новой игры или /restart для перезапуска";
            }

            sendMessage.setText(message);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String receiveInput() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
        if (gameStarted) {
            showStartMessage();
            inputQueue.clear(); // Очищаем очередь перед началом новой игры
            inputQueue.offer(""); // Добавляем пустую команду, чтобы запустить игру
        }
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }
}