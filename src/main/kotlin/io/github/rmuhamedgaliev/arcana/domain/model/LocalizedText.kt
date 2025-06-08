package io.github.rmuhamedgaliev.arcana.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Class for storing text in multiple languages.
 * Supports variable substitution in text.
 */
class LocalizedText {
    private val texts: MutableMap<Language, String> = mutableMapOf()
    
    /**
     * Set text for a language.
     *
     * @param language The language
     * @param text The text
     */
    fun setText(language: Language, text: String) {
        texts[language] = text
    }
    
    /**
     * Get text for a language.
     *
     * @param language The language
     * @return The text, or null if not set
     */
    fun getText(language: Language): String? {
        return texts[language]
    }
    
    /**
     * Get text for a language with variable substitution.
     *
     * @param language The language
     * @param variables The variables to substitute
     * @return The text with variables substituted, or null if not set
     */
    fun getText(language: Language, variables: Map<String, Any>): String? {
        val text = texts[language] ?: return null
        
        return text.replace(Regex("\\{([^}]+)\\}")) { matchResult ->
            val variableName = matchResult.groupValues[1]
            variables[variableName]?.toString() ?: "{$variableName}"
        }
    }
    
    /**
     * Check if text is set for a language.
     *
     * @param language The language
     * @return True if text is set, false otherwise
     */
    fun hasText(language: Language): Boolean {
        return texts.containsKey(language)
    }
    
    /**
     * Get all texts.
     *
     * @return A map of all texts
     */
    @JsonIgnore
    fun getAllTexts(): Map<Language, String> {
        return texts.toMap()
    }
    
    /**
     * Get text for a language, or a fallback language if not set.
     *
     * @param language The language
     * @param fallbackLanguage The fallback language
     * @return The text, or null if not set in either language
     */
    fun getTextWithFallback(language: Language, fallbackLanguage: Language = Language.EN): String? {
        return texts[language] ?: texts[fallbackLanguage]
    }
    
    /**
     * Get text for a language with variable substitution, or a fallback language if not set.
     *
     * @param language The language
     * @param variables The variables to substitute
     * @param fallbackLanguage The fallback language
     * @return The text with variables substituted, or null if not set in either language
     */
    fun getTextWithFallback(language: Language, variables: Map<String, Any>, fallbackLanguage: Language = Language.EN): String? {
        val text = texts[language] ?: texts[fallbackLanguage] ?: return null
        
        return text.replace(Regex("\\{([^}]+)\\}")) { matchResult ->
            val variableName = matchResult.groupValues[1]
            variables[variableName]?.toString() ?: "{$variableName}"
        }
    }
}
