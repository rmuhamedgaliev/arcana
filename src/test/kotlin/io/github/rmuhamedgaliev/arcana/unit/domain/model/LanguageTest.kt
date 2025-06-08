package io.github.rmuhamedgaliev.arcana.unit.domain.model

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguageTest {

    @Test
    fun `should return correct language code and display name`() {
        // Given
        val english = Language.EN
        val russian = Language.RU

        // Then
        assertEquals("en", english.code)
        assertEquals("English", english.displayName)
        assertEquals("ru", russian.code)
        assertEquals("Русский", russian.displayName)
    }

    @Test
    fun `should find language by code`() {
        // Given
        val englishCode = "en"
        val russianCode = "ru"

        // When
        val english = Language.fromCode(englishCode)
        val russian = Language.fromCode(russianCode)

        // Then
        assertEquals(Language.EN, english)
        assertEquals(Language.RU, russian)
    }

    @Test
    fun `should find language by code case insensitive`() {
        // Given
        val englishCode = "EN"
        val russianCode = "RU"

        // When
        val english = Language.fromCode(englishCode)
        val russian = Language.fromCode(russianCode)

        // Then
        assertEquals(Language.EN, english)
        assertEquals(Language.RU, russian)
    }

    @Test
    fun `should return null for unknown language code`() {
        // Given
        val unknownCode = "fr"

        // When
        val language = Language.fromCode(unknownCode)

        // Then
        assertNull(language)
    }
}
