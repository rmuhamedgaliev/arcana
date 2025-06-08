package io.github.rmuhamedgaliev.arcana.unit.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.story.Ending
import io.github.rmuhamedgaliev.arcana.domain.model.story.EndingAnalyzer
import io.github.rmuhamedgaliev.arcana.domain.model.story.EndingCategory
import io.github.rmuhamedgaliev.arcana.domain.model.story.EndingRarity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndingAnalyzerTest {

    private lateinit var analyzer: EndingAnalyzer
    private lateinit var heroicEnding: Ending
    private lateinit var tragicEnding: Ending
    private lateinit var secretEnding: Ending
    private lateinit var dependentEnding: Ending
    private lateinit var endings: List<Ending>

    @BeforeEach
    fun setUp() {
        analyzer = EndingAnalyzer()

        val createLocalizedText = { en: String, ru: String ->
            LocalizedText().apply {
                setText(Language.EN, en)
                setText(Language.RU, ru)
            }
        }

        heroicEnding = Ending(
            id = "hero_ending",
            beatId = "final_battle",
            title = createLocalizedText("Hero's Victory", "Победа героя"),
            description = createLocalizedText("You saved the kingdom", "Вы спасли королевство"),
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.COMMON
        )

        tragicEnding = Ending(
            id = "tragic_ending",
            beatId = "final_defeat",
            title = createLocalizedText("Hero's Sacrifice", "Жертва героя"),
            description = createLocalizedText("You sacrificed yourself", "Вы пожертвовали собой"),
            category = EndingCategory.TRAGIC,
            rarity = EndingRarity.UNCOMMON
        )

        secretEnding = Ending(
            id = "secret_ending",
            beatId = "hidden_path",
            title = createLocalizedText("Secret Alliance", "Тайный союз"),
            description = createLocalizedText("You formed a secret alliance", "Вы сформировали тайный союз"),
            category = EndingCategory.SECRET,
            rarity = EndingRarity.VERY_RARE
        )

        dependentEnding = Ending(
            id = "dependent_ending",
            beatId = "epilogue",
            title = createLocalizedText("New Beginning", "Новое начало"),
            description = createLocalizedText("A new chapter begins", "Начинается новая глава"),
            category = EndingCategory.SPECIAL,
            rarity = EndingRarity.LEGENDARY,
            requirements = "ending:hero_ending AND ending:secret_ending"
        )

        endings = listOf(heroicEnding, tragicEnding, secretEnding, dependentEnding)
    }

    @Test
    fun `should get dependency graph of endings`() {
        // When
        val dependencyGraph = analyzer.getDependencyGraph(endings)

        // Then
        assertEquals(4, dependencyGraph.size)
        assertEquals(1, dependencyGraph["hero_ending"]?.size)
        assertEquals(0, dependencyGraph["tragic_ending"]?.size)
        assertEquals(1, dependencyGraph["secret_ending"]?.size)
        assertEquals(0, dependencyGraph["dependent_ending"]?.size)
        assertTrue(dependencyGraph["hero_ending"]?.contains("dependent_ending") ?: false)
        assertTrue(dependencyGraph["secret_ending"]?.contains("dependent_ending") ?: false)
    }

    @Test
    fun `should get rarity distribution of endings`() {
        // When
        val rarityDistribution = analyzer.getRarityDistribution(endings)

        // Then
        assertEquals(5, rarityDistribution.size) // All rarity types
        assertEquals(1, rarityDistribution[EndingRarity.COMMON])
        assertEquals(1, rarityDistribution[EndingRarity.UNCOMMON])
        assertEquals(0, rarityDistribution[EndingRarity.RARE])
        assertEquals(1, rarityDistribution[EndingRarity.VERY_RARE])
        assertEquals(1, rarityDistribution[EndingRarity.LEGENDARY])
    }

    @Test
    fun `should get category distribution of endings`() {
        // When
        val categoryDistribution = analyzer.getCategoryDistribution(endings)

        // Then
        assertEquals(6, categoryDistribution.size) // All category types
        assertEquals(1, categoryDistribution[EndingCategory.HEROIC])
        assertEquals(1, categoryDistribution[EndingCategory.TRAGIC])
        assertEquals(0, categoryDistribution[EndingCategory.NEUTRAL])
        assertEquals(0, categoryDistribution[EndingCategory.EVIL])
        assertEquals(1, categoryDistribution[EndingCategory.SECRET])
        assertEquals(1, categoryDistribution[EndingCategory.SPECIAL])
    }

    @Test
    fun `should get hints for undiscovered endings`() {
        // Given
        val discoveredEndingIds = setOf("hero_ending", "tragic_ending")

        // When
        val hints = analyzer.getHintsForUndiscoveredEndings(endings, discoveredEndingIds)

        // Then
        assertEquals(2, hints.size)
        assertTrue(hints.any { it.contains("secret") && it.contains("very_rare") })
        assertTrue(hints.any { it.contains("special") && it.contains("legendary") })
    }

    @Test
    fun `should return empty list when all endings discovered`() {
        // Given
        val allEndingIds = endings.map { it.id }.toSet()

        // When
        val hints = analyzer.getHintsForUndiscoveredEndings(endings, allEndingIds)

        // Then
        assertTrue(hints.isEmpty())
    }

    @Test
    fun `should handle empty endings list`() {
        // Given
        val emptyEndings = emptyList<Ending>()

        // When
        val dependencyGraph = analyzer.getDependencyGraph(emptyEndings)
        val rarityDistribution = analyzer.getRarityDistribution(emptyEndings)
        val categoryDistribution = analyzer.getCategoryDistribution(emptyEndings)
        val hints = analyzer.getHintsForUndiscoveredEndings(emptyEndings, emptySet())

        // Then
        assertTrue(dependencyGraph.isEmpty())
        assertEquals(5, rarityDistribution.size) // All rarity types with 0 count
        assertEquals(6, categoryDistribution.size) // All category types with 0 count
        assertTrue(hints.isEmpty())
    }
}
