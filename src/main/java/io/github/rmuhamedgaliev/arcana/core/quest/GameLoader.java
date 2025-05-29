package io.github.rmuhamedgaliev.arcana.core.quest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rmuhamedgaliev.arcana.core.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for loading games from JSON files.
 */
public class GameLoader {

    /**
     * Load a game from a JSON file.
     *
     * @param filePath The path to the JSON file
     * @return The loaded game data
     * @throws IOException If an I/O error occurs
     */
    public static GameData loadGameData(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), GameData.class);
    }

    /**
     * Convert game data to a game object.
     *
     * @param gameData The game data
     * @return The game object
     */
    public static Game convertToGame(GameData gameData) {
        // Create localized title and description
        LocalizedText title = new LocalizedText();
        LocalizedText description = new LocalizedText();

        for (LocalizedGameData localization : gameData.getLocalizations()) {
            Language language = Language.valueOf(localization.getLanguage().toUpperCase());
            title.setText(language, localization.getTitle());
            description.setText(language, localization.getDescription());
        }

        // Create the game
        Game game = new Game(gameData.getId(), title, description, gameData.getStartQuestId());

        // Create scenes for each quest
        for (QuestData questData : gameData.getQuests()) {
            // Create localized text for the quest
            LocalizedText questText = new LocalizedText();
            for (LocalizedGameData localization : gameData.getLocalizations()) {
                Language language = Language.valueOf(localization.getLanguage().toUpperCase());
                String text = localization.getQuestText(questData.getId());
                if (text != null) {
                    questText.setText(language, text);
                }
            }

            // Create options for the quest
            List<GameOption> options = new ArrayList<>();
            if (!questData.isEnd()) {
                for (int i = 0; i < questData.getActions().size(); i++) {
                    ActionData actionData = questData.getActions().get(i);

                    // Create localized text for the action
                    LocalizedText actionText = new LocalizedText();
                    for (LocalizedGameData localization : gameData.getLocalizations()) {
                        Language language = Language.valueOf(localization.getLanguage().toUpperCase());
                        String text = localization.getActionText(questData.getId(), String.valueOf(i));
                        if (text != null) {
                            actionText.setText(language, text);
                        } else {
                            // Fallback to action text from quest data
                            actionText.setText(language, actionData.getText());
                        }
                    }

                    // Create the option
                    GameOption option = new GameOption(actionText, actionData.getNextScene(), actionData.getCondition());
                    options.add(option);
                }
            }

            // Create the scene with attributes
            Map<String, String> attributes = questData.getAttributes();
            GameScene scene = new GameScene(questData.getId(), questText, options, questData.isEnd(), attributes);
            game.addScene(scene);
        }

        return game;
    }

    /**
     * Load all games from a directory.
     *
     * @param directoryPath The path to the directory
     * @return A list of loaded games
     * @throws IOException If an I/O error occurs
     */
    public static List<Game> loadAllGames(String directoryPath) throws IOException {
        List<Game> games = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            List<Path> jsonFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toList());

            for (Path jsonFile : jsonFiles) {
                try {
                    GameData gameData = loadGameData(jsonFile.toString());
                    Game game = convertToGame(gameData);
                    games.add(game);
                } catch (Exception e) {
                    System.err.println("Error loading game from " + jsonFile + ": " + e.getMessage());
                }
            }
        }

        return games;
    }
}
