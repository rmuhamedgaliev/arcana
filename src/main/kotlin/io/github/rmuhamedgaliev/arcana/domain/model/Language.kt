package io.github.rmuhamedgaliev.arcana.domain.model

/**
 * Enum representing supported languages.
 */
enum class Language(val code: String, val displayName: String) {
    EN("en", "English"),
    RU("ru", "Русский");

    companion object {
        /**
         * Get a language by its code.
         *
         * @param code The language code
         * @return The language, or null if not found
         */
        fun fromCode(code: String): Language? {
            return values().find { it.code.equals(code, ignoreCase = true) }
        }
    }
}
