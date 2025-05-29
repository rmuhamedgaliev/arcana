package io.github.rmuhamedgaliev.arcana.core;

import io.github.rmuhamedgaliev.arcana.core.player.Player;
import io.github.rmuhamedgaliev.arcana.core.ports.GameInputPort;
import io.github.rmuhamedgaliev.arcana.core.ports.GameOutputPort;
import io.github.rmuhamedgaliev.arcana.core.quest.GameData;
import io.github.rmuhamedgaliev.arcana.core.quest.GameLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for handling the game logic.
 */
public class GameEngine implements GameInputPort {
    private final GameContext context;
    private final GameOutputPort gameOutputPort;
    private volatile boolean running;

    /**
     * Create a new game engine.
     *
     * @param context       The game context
     * @param gameOutputPort The game output port
     */
    public GameEngine(GameContext context, GameOutputPort gameOutputPort) {
        this.context = context;
        this.gameOutputPort = gameOutputPort;
        this.running = true;
    }

    /**
     * Stop the game engine.
     * This will cause the game loop to exit.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Get the game context.
     *
     * @return The game context
     */
    @Override
    public GameContext getContext() {
        return context;
    }

    /**
     * Start the game.
     */
    public void start() {
        // Set the language
        selectLanguage();

        // Load saved progress if available
        if (!context.loadProgress()) {
            // If no saved progress, show the game selection
            selectGame();
        }

        // Show initial player status
        printPlayerStatus();

        // Start the game loop
        gameLoop();
    }

    /**
     * Show the language selection.
     */
    private void selectLanguage() {
        try {
            List<String> options = Arrays.stream(Language.values())
                    .map(Language::getDisplayName)
                    .collect(Collectors.toList());

            int selectedIndex = gameOutputPort.sendOptionsMessage("Select language / Выберите язык:", options);
            Language selectedLanguage = Language.values()[selectedIndex];

            context.setCurrentLanguage(selectedLanguage);
            gameOutputPort.setCurrentLanguage(selectedLanguage);
        } catch (RuntimeException e) {
            System.err.println("Error in language selection: " + e.getMessage());
            gameOutputPort.sendMessage("Error selecting language. Using English as default.");
            context.setCurrentLanguage(Language.EN);
            gameOutputPort.setCurrentLanguage(Language.EN);
        }
    }

    /**
     * Show the game selection.
     */
    private void selectGame() {
        try {
            Map<String, Game> games = context.getAvailableGames();

            if (games.isEmpty()) {
                gameOutputPort.sendMessage("No games available.");
                return;
            }

            List<String> options = new ArrayList<>();
            List<Game> gameList = new ArrayList<>();

            for (Game game : games.values()) {
                options.add(game.getTitle().getText(context.getCurrentLanguage()));
                gameList.add(game);
            }

            int selectedIndex = gameOutputPort.sendOptionsMessage("Select a game:", options);
            Game selectedGame = gameList.get(selectedIndex);

            context.setCurrentGame(selectedGame);
        } catch (RuntimeException e) {
            System.err.println("Error in game selection: " + e.getMessage());
            gameOutputPort.sendMessage("Error selecting game. Game over.");
            // Set current game to null to end the game
            context.setCurrentGame(null);
        }
    }

    /**
     * The main game loop.
     */
    private void gameLoop() {
        while (running) {
            try {
                GameScene currentScene = context.getCurrentScene();
                if (currentScene == null) {
                    gameOutputPort.sendMessage("Game over.");
                    break;
                }

                // Display the scene text
                String sceneText = currentScene.getText().getText(context.getCurrentLanguage());

                if (currentScene.isEndScene()) {
                    // End scene, no options
                    gameOutputPort.sendMessage(sceneText);
                    gameOutputPort.sendMessage("Game over.");
                    break;
                } else {
                    // Get valid options based on conditions
                    List<GameOption> validOptions = getValidOptions(currentScene);

                    if (validOptions.isEmpty()) {
                        gameOutputPort.sendMessage(sceneText);
                        gameOutputPort.sendMessage("No valid options available. Game over.");
                        break;
                    }

                    // Display options
                    List<String> optionTexts = validOptions.stream()
                            .map(option -> option.getText().getText(context.getCurrentLanguage()))
                            .collect(Collectors.toList());

                    try {
                        int selectedIndex = gameOutputPort.sendOptionsMessage(sceneText, optionTexts);
                        GameOption selectedOption = validOptions.get(selectedIndex);

                        // Move to the next scene
                        String nextSceneId = selectedOption.getNextSceneId();
                        GameScene nextScene = context.getCurrentGame().getScene(nextSceneId);
                        context.setCurrentScene(nextScene);

                        // Apply attributes from the scene to the player
                        applySceneAttributes(nextScene);

                        // Check if player health is below or equal to 0
                        if (context.getPlayer().hasAttribute("health") && 
                            context.getPlayer().getAttribute("health") <= 0) {
                            gameOutputPort.sendMessage("Your health has dropped to 0 or below. Game over.");
                            break;
                        }

                        // Print player status
                        printPlayerStatus();

                        // Save progress
                        context.saveProgress();
                    } catch (RuntimeException e) {
                        // If there's an error getting user input (e.g., timeout), end the game
                        System.err.println("Error in game loop: " + e.getMessage());
                        gameOutputPort.sendMessage("Game interrupted due to an error or timeout.");
                        break;
                    }
                }
            } catch (Exception e) {
                // Catch any other exceptions to prevent the game from crashing
                System.err.println("Unexpected error in game loop: " + e.getMessage());
                gameOutputPort.sendMessage("An unexpected error occurred. Game over.");
                break;
            }
        }
    }

    /**
     * Get valid options based on conditions.
     *
     * @param scene The scene
     * @return The list of valid options
     */
    private List<GameOption> getValidOptions(GameScene scene) {
        Player player = context.getPlayer();

        return scene.getOptions().stream()
                .filter(option -> {
                    if (!option.hasCondition()) {
                        return true;
                    }

                    // Simple condition evaluation (format: "attribute >= value")
                    String condition = option.getCondition();
                    String[] parts = condition.split("\\s+");

                    if (parts.length != 3) {
                        return false;
                    }

                    String attribute = parts[0];
                    String operator = parts[1];
                    int value;

                    try {
                        value = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                    int playerValue = player.getAttribute(attribute);

                    switch (operator) {
                        case "==": return playerValue == value;
                        case "!=": return playerValue != value;
                        case ">": return playerValue > value;
                        case "<": return playerValue < value;
                        case ">=": return playerValue >= value;
                        case "<=": return playerValue <= value;
                        default: return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply attributes from a scene to the player.
     *
     * @param scene The scene
     */
    private void applySceneAttributes(GameScene scene) {
        Player player = context.getPlayer();
        Map<String, String> attributes = scene.getAttributes();

        // Apply each attribute to the player
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                // Check if the value starts with + or - for relative changes
                if (value.startsWith("+")) {
                    int currentValue = player.getAttribute(key);
                    int change = Integer.parseInt(value.substring(1));
                    player.setAttribute(key, currentValue + change);
                } else if (value.startsWith("-")) {
                    int currentValue = player.getAttribute(key);
                    int change = Integer.parseInt(value.substring(1));
                    player.setAttribute(key, currentValue - change);
                } else {
                    // Absolute value
                    player.setAttribute(key, Integer.parseInt(value));
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing attribute value: " + value);
            }
        }
    }

    /**
     * Print the player's status.
     */
    private void printPlayerStatus() {
        Player player = context.getPlayer();
        Map<String, Integer> attributes = player.getAllAttributes();

        if (attributes.isEmpty()) {
            return;
        }

        gameOutputPort.displayPlayerStatus(attributes);
    }


    /**
     * Load quests from game data.
     *
     * @param gameData The game data
     */
    public void loadQuests(GameData gameData) {
        Game game = GameLoader.convertToGame(gameData);
        context.addGame(game);
    }

    /**
     * Load all games from a directory.
     *
     * @param directoryPath The path to the directory
     * @throws IOException If an I/O error occurs
     */
    public void loadAllGames(String directoryPath) throws IOException {
        List<Game> games = GameLoader.loadAllGames(directoryPath);
        for (Game game : games) {
            context.addGame(game);
        }
    }
}
