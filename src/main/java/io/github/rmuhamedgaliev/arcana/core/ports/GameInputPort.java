package io.github.rmuhamedgaliev.arcana.core.ports;

import io.github.rmuhamedgaliev.arcana.core.GameContext;

import java.io.IOException;

/**
 * Input port interface for the game engine.
 * This interface defines methods for interacting with the game engine from the outside.
 */
public interface GameInputPort {
    /**
     * Start the game.
     */
    void start();

    /**
     * Load quests from a directory.
     *
     * @param directoryPath The path to the directory
     * @throws IOException If an I/O error occurs
     */
    void loadAllGames(String directoryPath) throws IOException;

    /**
     * Get the game context.
     *
     * @return The game context
     */
    GameContext getContext();
}
