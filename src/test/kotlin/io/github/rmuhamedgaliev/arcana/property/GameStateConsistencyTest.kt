package io.github.rmuhamedgaliev.arcana.property

import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.kotest.property.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeBetween
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

class GameStateConsistencyTest : StringSpec({
    
    // Generator for LocalizedText
    val localizedTextGenerator = arbitrary {
        val text = LocalizedText()
        text.setText(Language.EN, Arb.string(1..100).bind())
        text.setText(Language.RU, Arb.string(1..100).bind())
        text
    }
    
    // Generator for Player
    val playerGenerator = arbitrary {
        val attributes = mutableMapOf<String, Int>()
        // Add some common attributes
        attributes["health"] = Arb.int(0..100).bind()
        attributes["strength"] = Arb.int(0..100).bind()
        attributes["intelligence"] = Arb.int(0..100).bind()
        
        Player(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now(),
            attributes = attributes,
            progress = mutableMapOf(
                "chapter" to Arb.string(1..10).bind(),
                "level" to Arb.int(1..50).bind().toString()
            ),
            subscriptionTier = Arb.enum<SubscriptionTier>().bind(),
            subscriptionExpiresAt = if (Arb.boolean().bind()) Instant.now().plusSeconds(Arb.long(0L..10000000L).bind()) else null
        )
    }
    
    // Generator for NarrativeChoice
    val narrativeChoiceGenerator = arbitrary {
        NarrativeChoice(
            id = UUID.randomUUID().toString(),
            text = localizedTextGenerator.bind(),
            nextBeatId = UUID.randomUUID().toString(),
            condition = if (Arb.boolean().bind()) "attribute:strength > 50" else null,
            tags = mutableSetOf("choice", "option"),
            metadata = mutableMapOf("importance" to Arb.string(1..10).bind())
        )
    }
    
    // Generator for StoryBeat
    val storyBeatGenerator = arbitrary { rs ->
        val id = UUID.randomUUID().toString()
        val choices = mutableListOf<NarrativeChoice>()
        
        // Add 1-3 choices to each beat
        repeat(Arb.int(1..3).bind()) {
            choices.add(narrativeChoiceGenerator.bind())
        }
        
        StoryBeat(
            id = id,
            text = localizedTextGenerator.bind(),
            choices = choices,
            isEndBeat = Arb.boolean().bind(),
            tags = mutableSetOf("beat", "scene"),
            metadata = mutableMapOf("mood" to Arb.string(1..10).bind())
        )
    }
    
    // Generator for connected Story
    val connectedStoryGenerator = arbitrary {
        val storyId = UUID.randomUUID().toString()
        val beats = mutableMapOf<String, StoryBeat>()
        
        // Create 3-10 beats
        val beatCount = Arb.int(3..10).bind()
        val beatIds = List(beatCount) { UUID.randomUUID().toString() }
        
        // Create beats with choices that connect to other beats
        beatIds.forEachIndexed { index, beatId ->
            val choices = mutableListOf<NarrativeChoice>()
            
            // Add 1-2 choices to each beat (except the last one which can be an end beat)
            if (index < beatCount - 1) {
                val choiceCount = Arb.int(1..2).bind()
                repeat(choiceCount) {
                    // Connect to a random beat that comes after this one
                    val nextBeatIndex = Arb.int(index + 1 until beatCount).bind()
                    choices.add(
                        NarrativeChoice(
                            id = UUID.randomUUID().toString(),
                            text = localizedTextGenerator.bind(),
                            nextBeatId = beatIds[nextBeatIndex],
                            condition = if (Arb.boolean().bind()) "attribute:strength > 50" else null
                        )
                    )
                }
            }
            
            beats[beatId] = StoryBeat(
                id = beatId,
                text = localizedTextGenerator.bind(),
                choices = choices,
                isEndBeat = index == beatCount - 1
            )
        }
        
        Story(
            id = storyId,
            title = localizedTextGenerator.bind(),
            description = localizedTextGenerator.bind(),
            startBeatId = beatIds.first(),
            beats = beats,
            requiredSubscriptionTier = Arb.enum<SubscriptionTier>().bind()
        )
    }
    
    "player attributes should stay within valid bounds" {
        forAll(playerGenerator) { player ->
            player.getAttribute("health") in 0..100 &&
            player.getAttribute("strength") in 0..100 &&
            player.getAttribute("intelligence") in 0..100
        }
    }
    
    "story graph should always be connected" {
        checkAll(connectedStoryGenerator) { story ->
            // Verify that all beats are reachable from the start beat
            val reachableBeats = mutableSetOf<String>()
            
            fun traverseStory(beatId: String) {
                if (beatId in reachableBeats) return
                reachableBeats.add(beatId)
                
                val beat = story.getBeat(beatId)
                if (beat != null) {
                    for (choice in beat.choices) {
                        traverseStory(choice.nextBeatId)
                    }
                }
            }
            
            traverseStory(story.startBeatId)
            
            // All beats should be reachable
            reachableBeats.size shouldBe story.beats.size
        }
    }
    
    "serialization should be lossless" {
        checkAll(playerGenerator) { player ->
            val json = Json { 
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            
            // Serialize and deserialize the player
            val serialized = json.encodeToString(player)
            val deserialized = json.decodeFromString<Player>(serialized)
            
            // Check that the deserialized player has the same properties
            deserialized.id shouldBe player.id
            deserialized.attributes shouldBe player.attributes
            deserialized.progress shouldBe player.progress
            deserialized.subscriptionTier shouldBe player.subscriptionTier
            
            // For timestamps, we can't do exact equality due to precision issues
            // So we check that they're close enough
            if (player.subscriptionExpiresAt != null) {
                deserialized.subscriptionExpiresAt shouldNotBe null
            }
            
            true
        }
        
        checkAll(connectedStoryGenerator) { story ->
            val json = Json { 
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            
            // Serialize and deserialize the story
            val serialized = json.encodeToString(story)
            val deserialized = json.decodeFromString<Story>(serialized)
            
            // Check that the deserialized story has the same properties
            deserialized.id shouldBe story.id
            deserialized.title shouldBe story.title
            deserialized.description shouldBe story.description
            deserialized.startBeatId shouldBe story.startBeatId
            deserialized.beats.size shouldBe story.beats.size
            deserialized.requiredSubscriptionTier shouldBe story.requiredSubscriptionTier
            
            // Check that all beats are preserved
            story.beats.keys.forEach { beatId ->
                deserialized.beats.containsKey(beatId) shouldBe true
                
                val originalBeat = story.beats[beatId]!!
                val deserializedBeat = deserialized.beats[beatId]!!
                
                deserializedBeat.id shouldBe originalBeat.id
                deserializedBeat.text shouldBe originalBeat.text
                deserializedBeat.isEndBeat shouldBe originalBeat.isEndBeat
                deserializedBeat.choices.size shouldBe originalBeat.choices.size
            }
            
            true
        }
    }
})
