package io.github.rmuhamedgaliev.arcana;

import io.github.rmuhamedgaliev.arcana.core.Game;
import io.github.rmuhamedgaliev.arcana.core.Language;
import io.github.rmuhamedgaliev.arcana.core.quest.GameLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameLoaderTest {

    @Test
    public void testLoadGamesFromRelativePath() throws IOException {
        System.out.println("[DEBUG_LOG] Testing game loading from 'games' directory");
        List<Game> games = GameLoader.loadAllGames("games");
        System.out.println("[DEBUG_LOG] Found " + games.size() + " games");

        assertFalse(games.isEmpty(), "Should find at least one game");

        for (Game game : games) {
            System.out.println("[DEBUG_LOG] Game ID: " + game.getId());
            System.out.println("[DEBUG_LOG] Game Title: " + game.getTitle().getText(Language.EN));
            System.out.println("[DEBUG_LOG] Start Scene ID: " + game.getStartSceneId());
            System.out.println("[DEBUG_LOG] Number of scenes: " + game.getAllScenes().size());

            assertNotNull(game.getId(), "Game ID should not be null");
            assertNotNull(game.getTitle(), "Game title should not be null");
            assertFalse(game.getAllScenes().isEmpty(), "Game should have at least one scene");
        }
    }

    @Test
    public void testLoadGamesFromAbsolutePath() throws IOException {
        String absolutePath = "/home/rmuhamedgaliev/Projects/arcana/games";
        System.out.println("[DEBUG_LOG] Testing game loading from absolute path: " + absolutePath);
        List<Game> games = GameLoader.loadAllGames(absolutePath);
        System.out.println("[DEBUG_LOG] Found " + games.size() + " games from absolute path");

        assertFalse(games.isEmpty(), "Should find at least one game from absolute path");
    }
}
