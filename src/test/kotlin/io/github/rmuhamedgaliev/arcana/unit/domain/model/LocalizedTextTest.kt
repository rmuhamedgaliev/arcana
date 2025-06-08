package io.github.rmuhamedgaliev.arcana.unit.domain.model

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalizedTextTest {

    private lateinit var localizedText: LocalizedText

    @BeforeEach
    fun setUp() {
        localizedText = LocalizedText()
    }

    @Test
    fun `should set and get text for a language`() {
        // Given
        val englishText = "Hello, world!"
        val russianText = "Привет, мир!"

        // When
        localizedText.setText(Language.EN, englishText)
        localizedText.setText(Language.RU, russianText)

        // Then
        assertEquals(englishText, localizedText.getText(Language.EN))
        assertEquals(russianText, localizedText.getText(Language.RU))
    }

    @Test
    fun `should return null for language with no text`() {
        // Given
        localizedText.setText(Language.EN, "Hello, world!")

        // When
        val result = localizedText.getText(Language.RU)

        // Then
        assertNull(result)
    }

    @Test
    fun `should substitute variables in text`() {
        // Given
        localizedText.setText(Language.EN, "Hello, {name}! Your score is {score}.")
        val variables = mapOf("name" to "John", "score" to 42)

        // When
        val result = localizedText.getText(Language.EN, variables)

        // Then
        assertEquals("Hello, John! Your score is 42.", result)
    }

    @Test
    fun `should keep variable placeholder if variable not provided`() {
        // Given
        localizedText.setText(Language.EN, "Hello, {name}! Your score is {score}.")
        val variables = mapOf("name" to "John")

        // When
        val result = localizedText.getText(Language.EN, variables)

        // Then
        assertEquals("Hello, John! Your score is {score}.", result)
    }

    @Test
    fun `should return null when substituting variables for language with no text`() {
        // Given
        localizedText.setText(Language.EN, "Hello, {name}!")
        val variables = mapOf("name" to "John")

        // When
        val result = localizedText.getText(Language.RU, variables)

        // Then
        assertNull(result)
    }

    @Test
    fun `should check if text is set for a language`() {
        // Given
        localizedText.setText(Language.EN, "Hello, world!")

        // When
        val hasEnglish = localizedText.hasText(Language.EN)
        val hasRussian = localizedText.hasText(Language.RU)

        // Then
        assertTrue(hasEnglish)
        assertFalse(hasRussian)
    }

    @Test
    fun `should get all texts`() {
        // Given
        val englishText = "Hello, world!"
        val russianText = "Привет, мир!"
        localizedText.setText(Language.EN, englishText)
        localizedText.setText(Language.RU, russianText)

        // When
        val allTexts = localizedText.getAllTexts()

        // Then
        assertEquals(2, allTexts.size)
        assertEquals(englishText, allTexts[Language.EN])
        assertEquals(russianText, allTexts[Language.RU])
    }

    @Test
    fun `should get text with fallback language`() {
        // Given
        localizedText.setText(Language.EN, "Hello, world!")

        // When
        val result = localizedText.getTextWithFallback(Language.RU, Language.EN)

        // Then
        assertEquals("Hello, world!", result)
    }

    @Test
    fun `should return null when text not available in primary or fallback language`() {
        // Given
        // No text set

        // When
        val result = localizedText.getTextWithFallback(Language.RU, Language.EN)

        // Then
        assertNull(result)
    }

    @Test
    fun `should substitute variables with fallback language`() {
        // Given
        localizedText.setText(Language.EN, "Hello, {name}!")
        val variables = mapOf("name" to "John")

        // When
        val result = localizedText.getTextWithFallback(Language.RU, variables, Language.EN)

        // Then
        assertEquals("Hello, John!", result)
    }

    @Test
    fun `should return null when substituting variables with no text in primary or fallback language`() {
        // Given
        // No text set
        val variables = mapOf("name" to "John")

        // When
        val result = localizedText.getTextWithFallback(Language.RU, variables, Language.EN)

        // Then
        assertNull(result)
    }
}
