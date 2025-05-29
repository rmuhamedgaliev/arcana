package io.github.rmuhamedgaliev.arcana.core;

/**
 * Enum representing supported languages in the game.
 */
public enum Language {
    EN("English"),
    RU("Русский");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
