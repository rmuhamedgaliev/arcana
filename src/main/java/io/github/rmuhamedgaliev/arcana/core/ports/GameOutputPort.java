package io.github.rmuhamedgaliev.arcana.core.ports;

import io.github.rmuhamedgaliev.arcana.core.Language;

import java.util.List;
import java.util.Map;

/**
 * Output port interface for the game engine.
 * This interface defines methods for the game engine to interact with the outside world (UI).
 */
public interface GameOutputPort {
    /**
     * Send a message to the user.
     *
     * @param message The message to send
     */
    void sendMessage(String message);

    /**
     * Send a message with options to the user.
     *
     * @param message The message to send
     * @param options The list of options for the user to choose from
     * @return The index of the selected option
     */
    int sendOptionsMessage(String message, List<String> options);

    /**
     * Get the current language selected by the user.
     *
     * @return The current language
     */
    Language getCurrentLanguage();

    /**
     * Set the current language for the user.
     *
     * @param language The language to set
     */
    void setCurrentLanguage(Language language);

    /**
     * Display player status.
     *
     * @param attributes The player attributes to display
     */
    void displayPlayerStatus(Map<String, Integer> attributes);
}
