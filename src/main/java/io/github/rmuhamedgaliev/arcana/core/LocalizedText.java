package io.github.rmuhamedgaliev.arcana.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling text in multiple languages.
 */
public class LocalizedText {
    private final Map<Language, String> texts;
    private Language defaultLanguage;

    public LocalizedText() {
        this.texts = new HashMap<>();
        this.defaultLanguage = Language.EN;
    }

    public LocalizedText(Map<Language, String> texts) {
        this.texts = new HashMap<>(texts);
        this.defaultLanguage = Language.EN;
    }

    public void setText(Language language, String text) {
        texts.put(language, text);
    }

    public String getText(Language language) {
        String text = texts.get(language);
        if (text == null) {
            // Fallback to default language if text is not available in requested language
            text = texts.get(defaultLanguage);
            if (text == null && !texts.isEmpty()) {
                // Fallback to any available language if text is not available in default language
                text = texts.values().iterator().next();
            }
        }
        return text != null ? text : "";
    }

    public void setDefaultLanguage(Language defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    public boolean hasText(Language language) {
        return texts.containsKey(language);
    }

    public Map<Language, String> getAllTexts() {
        return new HashMap<>(texts);
    }
}
