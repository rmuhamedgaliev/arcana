package io.github.rmuhamedgaliev.arcana.unit.domain.model.story

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.story.Ending
import io.github.rmuhamedgaliev.arcana.domain.model.story.EndingCategory
import io.github.rmuhamedgaliev.arcana.domain.model.story.EndingRarity
import io.github.rmuhamedgaliev.arcana.domain.model.story.ending
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EndingTest {

    private lateinit var title: LocalizedText
    private lateinit var description: LocalizedText
    private lateinit var ending: Ending

    @BeforeEach
    fun setUp() {
        title = LocalizedText().apply {
            setText(Language.EN, "Hero's Victory")
            setText(Language.RU, "Победа героя")
        }

        description = LocalizedText().apply {
            setText(Language.EN, "You have saved the kingdom and become a hero.")
            setText(Language.RU, "Вы спасли королевство и стали героем.")
        }

        ending = Ending(
            id = "hero_ending",
            beatId = "final_battle",
            title = title,
            description = description,
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.RARE,
            requirements = "player.reputation > 80 AND has_defeated_dragon == true"
        )
    }

    @Test
    fun `should create ending with correct properties`() {
        // Given
        val id = "hero_ending"
        val beatId = "final_battle"
        val category = EndingCategory.HEROIC
        val rarity = EndingRarity.RARE
        val requirements = "player.reputation > 80 AND has_defeated_dragon == true"

        // Then
        assertEquals(id, ending.id)
        assertEquals(beatId, ending.beatId)
        assertEquals(title, ending.title)
        assertEquals(description, ending.description)
        assertEquals(category, ending.category)
        assertEquals(rarity, ending.rarity)
        assertEquals(requirements, ending.requirements)
        assertTrue(ending.unlocks.isEmpty())
        assertTrue(ending.metadata.isEmpty())
        assertTrue(ending.tags.isEmpty())
    }

    @Test
    fun `should add and retrieve metadata`() {
        // Given
        val ending = Ending(
            id = "hero_ending",
            beatId = "final_battle",
            title = title,
            description = description,
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.RARE
        )

        // When
        ending.addMetadata("achievement", "true_hero")
        ending.addMetadata("unlocks_story", "sequel_campaign")

        // Then
        assertEquals("true_hero", ending.getMetadata("achievement"))
        assertEquals("sequel_campaign", ending.getMetadata("unlocks_story"))
        assertNull(ending.getMetadata("nonexistent"))
    }

    @Test
    fun `should add and check tags`() {
        // Given
        val ending = Ending(
            id = "hero_ending",
            beatId = "final_battle",
            title = title,
            description = description,
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.RARE
        )

        // When
        ending.addTag("good")
        ending.addTag("dragon_slayer")

        // Then
        assertTrue(ending.hasTag("good"))
        assertTrue(ending.hasTag("dragon_slayer"))
        assertFalse(ending.hasTag("evil"))
    }

    @Test
    fun `should add and retrieve unlocks`() {
        // Given
        val ending = Ending(
            id = "hero_ending",
            beatId = "final_battle",
            title = title,
            description = description,
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.RARE
        )

        // When
        ending.addUnlock("achievement", "true_hero")
        ending.addUnlock("story", "sequel_campaign")

        // Then
        assertEquals("true_hero", ending.getUnlock("achievement"))
        assertEquals("sequel_campaign", ending.getUnlock("story"))
        assertNull(ending.getUnlock("nonexistent"))
        assertTrue(ending.hasUnlock("achievement"))
        assertTrue(ending.hasUnlock("story"))
        assertFalse(ending.hasUnlock("nonexistent"))
    }

    @Test
    fun `should create ending using DSL`() {
        // When
        val dslEnding = ending("hero_ending") {
            setBeatId("final_battle")
            
            title {
                en = "Hero's Victory"
                ru = "Победа героя"
            }
            
            description {
                en = "You have saved the kingdom and become a hero."
                ru = "Вы спасли королевство и стали героем."
            }
            
            category(EndingCategory.HEROIC)
            rarity(EndingRarity.RARE)
            requirements("player.reputation > 80 AND has_defeated_dragon == true")
            
            unlock("achievement", "true_hero")
            metadata("unlocks_story", "sequel_campaign")
            tag("good")
        }

        // Then
        assertEquals("hero_ending", dslEnding.id)
        assertEquals("final_battle", dslEnding.beatId)
        assertEquals("Hero's Victory", dslEnding.title.getText(Language.EN))
        assertEquals("Победа героя", dslEnding.title.getText(Language.RU))
        assertEquals("You have saved the kingdom and become a hero.", dslEnding.description.getText(Language.EN))
        assertEquals("Вы спасли королевство и стали героем.", dslEnding.description.getText(Language.RU))
        assertEquals(EndingCategory.HEROIC, dslEnding.category)
        assertEquals(EndingRarity.RARE, dslEnding.rarity)
        assertEquals("player.reputation > 80 AND has_defeated_dragon == true", dslEnding.requirements)
        assertEquals("true_hero", dslEnding.getUnlock("achievement"))
        assertEquals("sequel_campaign", dslEnding.getMetadata("unlocks_story"))
        assertTrue(dslEnding.hasTag("good"))
    }

    @Test
    fun `should generate UUID if id not provided`() {
        // When
        val endingWithoutId = Ending(
            beatId = "final_battle",
            title = title,
            description = description,
            category = EndingCategory.HEROIC,
            rarity = EndingRarity.RARE
        )

        // Then
        assertNotNull(endingWithoutId.id)
        assertTrue(endingWithoutId.id.isNotBlank())
    }

    @Test
    fun `should have correct percentage of players for each rarity`() {
        // Then
        assertEquals(50.0, EndingRarity.COMMON.percentageOfPlayers)
        assertEquals(25.0, EndingRarity.UNCOMMON.percentageOfPlayers)
        assertEquals(15.0, EndingRarity.RARE.percentageOfPlayers)
        assertEquals(8.0, EndingRarity.VERY_RARE.percentageOfPlayers)
        assertEquals(2.0, EndingRarity.LEGENDARY.percentageOfPlayers)
    }
}
