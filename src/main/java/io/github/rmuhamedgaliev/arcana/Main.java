package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.adapters.GameInterfaceAdapter;
import io.github.rmuhamedgaliev.arcana.core.GameContext;
import io.github.rmuhamedgaliev.arcana.core.GameEngine;
import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.ports.GameOutputPort;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;

/**
 * Main entry point for the application.
 * Can launch either the console interface or the Telegram bot interface.
 */
public class Main {
    public static void main(String[] args) {
        // Get configuration from environment variables with default values
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String gamesDirectory = System.getenv("GAMES_DIRECTORY");

        // Set default games directory if not provided
        if (gamesDirectory == null || gamesDirectory.isEmpty()) {
            gamesDirectory = "games";
        }

        // Check if arguments are provided via command line
        if (args.length >= 1) {
            botToken = args[0];
            if (args.length >= 2) {
                gamesDirectory = args[1];
            }
        }

        // If bot token is provided, launch Telegram bot interface
        if (botToken != null && !botToken.isEmpty()) {
            launchTelegramBot(botToken, gamesDirectory);
        } else {
            // Otherwise, launch console interface
            launchConsoleInterface(gamesDirectory);
        }
    }

    /**
     * Launch the Telegram bot interface.
     * 
     * @param botToken The Telegram bot token
     * @param gamesDirectory The directory containing the games
     */
    private static void launchTelegramBot(String botToken, String gamesDirectory) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramGameLauncher(botToken, gamesDirectory));
            System.out.println("Bot started successfully!");
            System.out.println("Games directory: " + gamesDirectory);
        } catch (TelegramApiException e) {
            System.err.println("Error starting bot: " + e.getMessage());
        }
    }

    /**
     * Launch the console interface.
     * 
     * @param gamesDirectory The directory containing the games
     */
    private static void launchConsoleInterface(String gamesDirectory) {
        Player player = new Player();
        GameContext context = new GameContext(player);
        ConsoleInterface consoleInterface = new ConsoleInterface();
        GameOutputPort gameOutputPort = new GameInterfaceAdapter(consoleInterface);
        GameEngine engine = new GameEngine(context, gameOutputPort);

        try {
            // Load all games from the games directory
            engine.loadAllGames(gamesDirectory);

            // Start the game
            engine.start();
        } catch (IOException e) {
            consoleInterface.sendMessage("Error loading games: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            consoleInterface.sendMessage("Critical error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
