package io.github.rmuhamedgaliev.arcana.telegram;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramInterface extends TelegramLongPollingBot implements GameInterface {
    private final String botToken;
    private final String botUsername;
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Long currentChatId;
    private boolean isGameStarted = false;
    private final Runnable restartAction;
    private static final Pattern OPTION_PATTERN = Pattern.compile("^(\\d+)\\.");

    public TelegramInterface(String botToken, String botUsername, Runnable restartAction) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.restartAction = restartAction;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            // Обработка нажатий на inline кнопки
            String callbackData = update.getCallbackQuery().getData();
            currentChatId = update.getCallbackQuery().getMessage().getChatId();

            if (isGameStarted) {
                inputQueue.offer(callbackData);
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            currentChatId = update.getMessage().getChatId();

            if (messageText.equals("/start") || messageText.equals("/restart")) {
                isGameStarted = false;
                restartAction.run();
                return;
            }

            if (!isGameStarted) {
                sendMessage("Используйте /start для начала игры или /restart для перезапуска");
            }
        }
    }

    private void showStartMessage() {
        SendMessage message = new SendMessage();
        message.setChatId(currentChatId.toString());
        message.setText("Добро пожаловать в игру!\n" +
                "Нажимайте на кнопки под сообщениями для выбора действий.\n" +
                "Игра начинается...");

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

            // Создаем inline кнопки для вариантов выбора
            InlineKeyboardMarkup markupInline = createInlineKeyboard(message);
            if (markupInline != null) {
                sendMessage.setReplyMarkup(markupInline);
            }

            sendMessage.setText(message);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(String message) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String[] lines = message.split("\n");
        boolean hasOptions = false;

        for (String line : lines) {
            Matcher matcher = OPTION_PATTERN.matcher(line);
            if (matcher.find()) {
                hasOptions = true;
                String number = matcher.group(1);
                String text = line.substring(line.indexOf(".") + 1).trim();

                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(text);
                button.setCallbackData(number);

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                rows.add(row);
            }
        }

        if (!hasOptions) {
            return null;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rows);
        return markupInline;
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
            inputQueue.clear();
            inputQueue.offer("");
        }
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }
}