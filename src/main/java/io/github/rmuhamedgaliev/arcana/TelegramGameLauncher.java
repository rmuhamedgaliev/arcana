package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.adapters.GameInterfaceAdapter;
import io.github.rmuhamedgaliev.arcana.core.GameContext;
import io.github.rmuhamedgaliev.arcana.core.GameEngine;
import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.player.PlayerRepository;
import io.github.rmuhamedgaliev.arcana.core.ports.GameOutputPort;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class for launching the game with the Telegram interface.
 */
public class TelegramGameLauncher extends TelegramLongPollingBot {
    private final String botToken;
    private final String gamesDirectory;
    private final Map<String, GameInstance> gameInstances;
    private final ExecutorService executorService;
    private final PlayerRepository playerRepository;

    // Menu command constants
    private static final String CMD_START = "/start";
    private static final String CMD_STOP = "/stop";
    private static final String CMD_NEW_GAME = "New Game";
    private static final String CMD_CONTINUE = "Continue Game";
    private static final String CMD_RESET = "Reset Progress";
    private static final String CMD_MENU = "Main Menu";

    /**
     * Create a new Telegram game launcher.
     *
     * @param botToken       The bot token
     * @param gamesDirectory The directory containing the games
     */
    public TelegramGameLauncher(String botToken, String gamesDirectory) {
        this.botToken = botToken;
        this.gamesDirectory = gamesDirectory;
        this.gameInstances = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.playerRepository = new PlayerRepository();

        // Initialize the database
        this.playerRepository.initDatabase();
    }

    @Override
    public String getBotUsername() {
        // Bot username is not needed for functionality, return a default value
        return "ArcanaBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String userId = update.getMessage().getFrom().getId().toString();
            String text = update.getMessage().getText();

            if (text.equals(CMD_START)) {
                // Show the main menu
                showMainMenu(chatId, userId);
            } else if (text.equals(CMD_STOP)) {
                // Stop the game instance for this chat
                stopGame(chatId);
                showMainMenu(chatId, userId);
            } else if (text.equals(CMD_NEW_GAME)) {
                // Start a new game instance for this chat
                startNewGame(chatId, userId);
            } else if (text.equals(CMD_CONTINUE)) {
                // Continue an existing game
                continueGame(chatId, userId);
            } else if (text.equals(CMD_RESET)) {
                // Reset player progress
                resetProgress(chatId, userId);
            } else if (text.equals(CMD_MENU)) {
                // Show the main menu
                showMainMenu(chatId, userId);
            } else {
                // Forward the update to the game instance
                GameInstance gameInstance = gameInstances.get(chatId);
                if (gameInstance != null) {
                    gameInstance.handleUpdate(update);
                } else {
                    showMainMenu(chatId, userId);
                }
            }
        }
    }

    /**
     * Show the main menu to the user.
     *
     * @param chatId The chat ID
     * @param userId The user ID
     */
    private void showMainMenu(String chatId, String userId) {
        // Check if the player exists in the database
        boolean hasExistingGame = playerRepository.loadPlayer(userId)
                .map(player -> player.hasProgress("currentGameId"))
                .orElse(false);

        // Create keyboard with appropriate options
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(CMD_NEW_GAME));
        keyboard.add(row1);

        if (hasExistingGame) {
            KeyboardRow row2 = new KeyboardRow();
            row2.add(new KeyboardButton(CMD_CONTINUE));
            keyboard.add(row2);

            KeyboardRow row3 = new KeyboardRow();
            row3.add(new KeyboardButton(CMD_RESET));
            keyboard.add(row3);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        // Send welcome message with keyboard
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Welcome to Arcana! Please select an option:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending menu: " + e.getMessage());
        }
    }

    /**
     * Start a new game instance for a chat.
     *
     * @param chatId The chat ID
     * @param userId The user ID
     */
    private void startNewGame(String chatId, String userId) {
        // Stop any existing game instance
        stopGame(chatId);

        // Create a new game instance with the user ID
        GameInstance gameInstance = new GameInstance(chatId, userId);
        gameInstances.put(chatId, gameInstance);

        // Start the game in a separate thread
        executorService.submit(gameInstance);

        sendMessage(chatId, "Starting a new game. Type /stop to stop the game or " + CMD_MENU + " to return to the menu.");
    }

    /**
     * Continue an existing game for a user.
     *
     * @param chatId The chat ID
     * @param userId The user ID
     */
    private void continueGame(String chatId, String userId) {
        // Stop any existing game instance
        stopGame(chatId);

        // Load the player from the database
        Optional<Player> playerOpt = playerRepository.loadPlayer(userId);

        if (playerOpt.isPresent()) {
            // Create a new game instance with the loaded player
            GameInstance gameInstance = new GameInstance(chatId, userId, playerOpt.get());
            gameInstances.put(chatId, gameInstance);

            // Start the game in a separate thread
            executorService.submit(gameInstance);

            sendMessage(chatId, "Continuing your game. Type /stop to stop the game or " + CMD_MENU + " to return to the menu.");
        } else {
            sendMessage(chatId, "No saved game found. Starting a new game.");
            startNewGame(chatId, userId);
        }
    }

    /**
     * Reset a player's progress.
     *
     * @param chatId The chat ID
     * @param userId The user ID
     */
    private void resetProgress(String chatId, String userId) {
        // Stop any existing game instance
        stopGame(chatId);

        // Reset the player's progress in the database
        playerRepository.resetPlayerProgress(userId);

        sendMessage(chatId, "Your progress has been reset.");
        showMainMenu(chatId, userId);
    }

    /**
     * Stop the game instance for a chat.
     *
     * @param chatId The chat ID
     */
    private void stopGame(String chatId) {
        GameInstance gameInstance = gameInstances.remove(chatId);
        if (gameInstance != null) {
            gameInstance.stop();
            sendMessage(chatId, "Game stopped.");
        }
    }

    /**
     * Send a message to a chat.
     *
     * @param chatId  The chat ID
     * @param message The message to send
     */
    private void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }


    /**
     * Class representing a game instance for a chat.
     */
    private class GameInstance implements Runnable {
        private final String chatId;
        private final String userId;
        private final TelegramInterface telegramInterface;
        private final GameEngine gameEngine;
        private final GameContext context;
        private volatile boolean running;

        /**
         * Create a new game instance with a new player.
         *
         * @param chatId The chat ID
         * @param userId The user ID
         */
        public GameInstance(String chatId, String userId) {
            this.chatId = chatId;
            this.userId = userId;
            this.running = true;

            // Create a player with the user ID
            Player player = new Player(userId);
            this.context = new GameContext(player);

            // Create the Telegram interface
            this.telegramInterface = new TelegramInterface(TelegramGameLauncher.this, chatId);

            // Create the game output port adapter
            GameOutputPort gameOutputPort = new GameInterfaceAdapter(telegramInterface);

            // Create the game engine
            this.gameEngine = new GameEngine(context, gameOutputPort);
        }

        /**
         * Create a game instance with an existing player.
         *
         * @param chatId The chat ID
         * @param userId The user ID
         * @param player The existing player
         */
        public GameInstance(String chatId, String userId, Player player) {
            this.chatId = chatId;
            this.userId = userId;
            this.running = true;

            // Use the provided player
            this.context = new GameContext(player);

            // Create the Telegram interface
            this.telegramInterface = new TelegramInterface(TelegramGameLauncher.this, chatId);

            // Create the game output port adapter
            GameOutputPort gameOutputPort = new GameInterfaceAdapter(telegramInterface);

            // Create the game engine
            this.gameEngine = new GameEngine(context, gameOutputPort);
        }

        @Override
        public void run() {
            try {
                // Load all games from the directory
                gameEngine.loadAllGames(gamesDirectory);

                // Try to load saved progress
                if (context.loadProgress()) {
                    telegramInterface.sendMessage("Continuing from your last saved position.");
                }

                // Start the game
                gameEngine.start();
            } catch (IOException e) {
                telegramInterface.sendMessage("Error loading games: " + e.getMessage());
            } catch (Exception e) {
                telegramInterface.sendMessage("Error: " + e.getMessage());
            } finally {
                // Save player progress before removing the instance
                savePlayerProgress();

                // Remove this game instance when it's done
                gameInstances.remove(chatId);
            }
        }

        /**
         * Handle an update from Telegram.
         *
         * @param update The update
         */
        public void handleUpdate(Update update) {
            if (running) {
                telegramInterface.handleUpdate(update);
            }
        }

        /**
         * Stop the game instance.
         */
        public void stop() {
            // Save player progress before stopping
            savePlayerProgress();
            running = false;
        }

        /**
         * Save the player's progress to the database.
         */
        private void savePlayerProgress() {
            try {
                System.out.println("Saving player progress for user: " + userId);

                // Save progress in the context
                context.saveProgress();
                System.out.println("Context progress saved. Current game: " + 
                    (context.getCurrentGame() != null ? context.getCurrentGame().getId() : "null") + 
                    ", Current scene: " + 
                    (context.getCurrentScene() != null ? context.getCurrentScene().getId() : "null"));

                // Save player to the database
                playerRepository.savePlayer(context.getPlayer());
                System.out.println("Player saved to database. Player ID: " + context.getPlayer().getId());
                System.out.println("Player progress: " + context.getPlayer().getAllProgress());
                System.out.println("Player attributes: " + context.getPlayer().getAllAttributes());
            } catch (Exception e) {
                System.err.println("Error saving player progress: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
