package io.github.rmuhamedgaliev.arcana.adapters;

import io.github.rmuhamedgaliev.arcana.core.GameInterface;
import io.github.rmuhamedgaliev.arcana.core.Language;
import io.github.rmuhamedgaliev.arcana.core.ports.GameOutputPort;

import java.util.List;
import java.util.Map;

/**
 * Adapter for GameInterface to implement GameOutputPort.
 * This allows using existing GameInterface implementations with the new architecture.
 */
public class GameInterfaceAdapter implements GameOutputPort {
    private final GameInterface gameInterface;

    /**
     * Create a new adapter for a GameInterface.
     *
     * @param gameInterface The GameInterface to adapt
     */
    public GameInterfaceAdapter(GameInterface gameInterface) {
        this.gameInterface = gameInterface;
    }

    @Override
    public void sendMessage(String message) {
        gameInterface.sendMessage(message);
    }

    @Override
    public int sendOptionsMessage(String message, List<String> options) {
        return gameInterface.sendOptionsMessage(message, options);
    }

    @Override
    public Language getCurrentLanguage() {
        return gameInterface.getCurrentLanguage();
    }

    @Override
    public void setCurrentLanguage(Language language) {
        gameInterface.setCurrentLanguage(language);
    }

    @Override
    public void displayPlayerStatus(Map<String, Integer> attributes) {
        if (attributes.isEmpty()) {
            return;
        }

        StringBuilder status = new StringBuilder("Current status: ");
        boolean first = true;

        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            if (!first) {
                status.append(", ");
            }
            status.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }

        gameInterface.sendMessage(status.toString());
    }
}
