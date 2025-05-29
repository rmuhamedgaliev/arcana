package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;
import io.github.rmuhamedgaliev.arcana.core.Language;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of GameInterface for Telegram interaction.
 */
public class TelegramInterface implements GameInterface {
    private final TelegramLongPollingBot bot;
    private final String chatId;
    private Language currentLanguage;
    private CompletableFuture<Integer> optionFuture;

    /**
     * Create a new Telegram interface.
     *
     * @param bot    The Telegram bot
     * @param chatId The chat ID
     */
    public TelegramInterface(TelegramLongPollingBot bot, String chatId) {
        this.bot = bot;
        this.chatId = chatId;
        this.currentLanguage = Language.EN;
        this.optionFuture = null; // Will be initialized when needed
    }

    @Override
    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    @Override
    public int sendOptionsMessage(String message, List<String> options) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        // Create keyboard with options
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        for (int i = 0; i < options.size(); i++) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton();
            button.setText((i + 1) + ". " + options.get(i));
            row.add(button);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            // Create a new future for this request
            optionFuture = new CompletableFuture<>();

            bot.execute(sendMessage);

            // Wait for the user to select an option with a timeout
            // This prevents the bot from getting stuck if the user doesn't respond
            try {
                // Wait for up to 1 hour for a response
                return optionFuture.get(1, TimeUnit.HOURS);
            } catch (TimeoutException e) {
                System.err.println("Timeout waiting for user response: " + e.getMessage());
                throw new RuntimeException("Timeout waiting for user response", e);
            }
        } catch (TelegramApiException | InterruptedException | ExecutionException e) {
            System.err.println("Error in sendOptionsMessage: " + e.getMessage());
            throw new RuntimeException("Error in sendOptionsMessage", e);
        }
    }

    @Override
    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    @Override
    public void setCurrentLanguage(Language language) {
        this.currentLanguage = language;
    }

    /**
     * Handle an update from Telegram.
     *
     * @param update The update
     */
    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();

            // Parse the option number (format: "1. Option text")
            if (text.matches("\\d+\\..*")) {
                try {
                    int optionNumber = Integer.parseInt(text.substring(0, text.indexOf('.')).trim());
                    // Only complete the future if it exists and is not already completed
                    if (optionFuture != null && !optionFuture.isDone()) {
                        optionFuture.complete(optionNumber - 1); // Convert to 0-based index
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing option number: " + e.getMessage());
                }
            }
        }
    }
}
