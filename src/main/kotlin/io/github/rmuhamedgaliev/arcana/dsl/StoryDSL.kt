package io.github.rmuhamedgaliev.arcana.dsl

import io.github.rmuhamedgaliev.arcana.domain.model.Language
import io.github.rmuhamedgaliev.arcana.domain.model.LocalizedText
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ChoiceWeight
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryArc
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat
import java.util.*

/**
 * DSL for creating stories.
 */
class StoryDSL {
    private lateinit var id: String
    private val title = LocalizedText()
    private val description = LocalizedText()
    private lateinit var startBeatId: String
    private var requiredSubscriptionTier: SubscriptionTier = SubscriptionTier.FREE
    private val tags = mutableSetOf<String>()
    private val metadata = mutableMapOf<String, String>()
    private val beats = mutableMapOf<String, StoryBeat>()
    private val arcs = mutableListOf<StoryArc>()

    /**
     * Set the ID of the story.
     */
    fun setId(id: String) {
        this.id = id
    }

    /**
     * Set the title of the story.
     */
    fun title(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(title)
    }

    /**
     * Set the description of the story.
     */
    fun description(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(description)
    }

    /**
     * Set the initial attributes of the player.
     */
    fun initialAttributes(init: AttributesDSL.() -> Unit) {
        val attributesDSL = AttributesDSL()
        attributesDSL.init()
        // Store initial attributes as metadata
        attributesDSL.attributes.forEach { (key, value) ->
            metadata["initial_attribute_$key"] = value.toString()
        }
    }

    /**
     * Set the required subscription tier for the story.
     */
    fun requiredSubscriptionTier(tier: SubscriptionTier) {
        this.requiredSubscriptionTier = tier
    }

    /**
     * Add a tag to the story.
     */
    fun tag(tag: String) {
        tags.add(tag)
    }

    /**
     * Add metadata to the story.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Define a story beat.
     */
    fun beat(id: String, init: BeatDSL.() -> Unit) {
        val beatDSL = BeatDSL(id)
        beatDSL.init()
        val beat = beatDSL.build()
        beats[id] = beat

        // If this is the first beat, set it as the start beat
        if (!::startBeatId.isInitialized) {
            startBeatId = id
        }
    }

    /**
     * Define a story arc.
     */
    fun arc(id: String, init: ArcDSL.() -> Unit) {
        val arcDSL = ArcDSL(id)
        arcDSL.init()
        val arc = arcDSL.build()
        arcs.add(arc)
    }

    /**
     * Build the story.
     */
    fun build(): Story {
        if (!::id.isInitialized) {
            id = UUID.randomUUID().toString()
        }

        if (!::startBeatId.isInitialized) {
            throw IllegalStateException("Story must have at least one beat")
        }

        val story = Story(
            id = id,
            title = title,
            description = description,
            startBeatId = startBeatId,
            requiredSubscriptionTier = requiredSubscriptionTier
        )

        // Add tags
        tags.forEach { story.addTag(it) }

        // Add metadata
        metadata.forEach { (key, value) -> story.addMetadata(key, value) }

        // Add beats
        beats.values.forEach { story.addBeat(it) }

        // Add arcs
        arcs.forEach { story.addArc(it) }

        return story
    }
}

/**
 * DSL for creating localized text.
 */
class LocalizedTextDSL {
    private val texts = mutableMapOf<Language, String>()

    /**
     * Set the text for English.
     */
    var en: String
        get() = texts[Language.EN] ?: ""
        set(value) {
            texts[Language.EN] = value
        }

    /**
     * Set the text for Russian.
     */
    var ru: String
        get() = texts[Language.RU] ?: ""
        set(value) {
            texts[Language.RU] = value
        }

    /**
     * Apply the localized text to a LocalizedText object.
     */
    fun applyTo(localizedText: LocalizedText) {
        texts.forEach { (language, text) ->
            localizedText.setText(language, text)
        }
    }
}

/**
 * DSL for creating attributes.
 */
class AttributesDSL {
    val attributes = mutableMapOf<String, Int>()

    /**
     * Set an attribute.
     */
    infix fun String.to(value: Int) {
        attributes[this] = value
    }

    /**
     * Alternative syntax for setting an attribute.
     */
    operator fun String.invoke(value: Int) {
        attributes[this] = value
    }
}

/**
 * DSL for creating story beats.
 */
class BeatDSL(private val id: String) {
    private val text = LocalizedText()
    private val choices = mutableListOf<NarrativeChoice>()
    private var isEndBeat = false
    private val attributes = mutableMapOf<String, String>()
    private val tags = mutableSetOf<String>()
    private val metadata = mutableMapOf<String, String>()

    /**
     * Set the text of the beat.
     */
    fun text(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(text)
    }

    /**
     * Define a choice.
     */
    fun choice(id: String, init: ChoiceDSL.() -> Unit) {
        val choiceDSL = ChoiceDSL(id)
        choiceDSL.init()
        val choice = choiceDSL.build()
        choices.add(choice)
    }

    /**
     * Set whether this is an end beat.
     */
    fun isEndBeat(value: Boolean = true) {
        isEndBeat = value
    }

    /**
     * Set an attribute.
     */
    fun attribute(key: String, value: String) {
        attributes[key] = value
    }

    /**
     * Add a tag to the beat.
     */
    fun tag(tag: String) {
        tags.add(tag)
    }

    /**
     * Add metadata to the beat.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Build the story beat.
     */
    fun build(): StoryBeat {
        val beat = StoryBeat(
            id = id,
            text = text,
            isEndBeat = isEndBeat
        )

        // Add choices
        choices.forEach { beat.addChoice(it) }

        // Add attributes
        attributes.forEach { (key, value) -> beat.addAttribute(key, value) }

        // Add tags
        tags.forEach { beat.addTag(it) }

        // Add metadata
        metadata.forEach { (key, value) -> beat.addMetadata(key, value) }

        return beat
    }
}

/**
 * DSL for creating choices.
 */
class ChoiceDSL(private val id: String) {
    private val text = LocalizedText()
    private lateinit var nextBeatId: String
    private var condition: String? = null
    private var weight: ChoiceWeight = ChoiceWeight.NORMAL
    private val consequences = mutableListOf<Consequence>()
    private val tags = mutableSetOf<String>()
    private val metadata = mutableMapOf<String, String>()

    /**
     * Set the text of the choice.
     */
    fun text(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(text)
    }

    /**
     * Set the next beat ID.
     */
    fun goto(beatId: String) {
        nextBeatId = beatId
    }

    /**
     * Set the condition for the choice.
     */
    fun condition(condition: String) {
        this.condition = condition
    }

    /**
     * Set the weight of the choice.
     */
    fun weight(weight: ChoiceWeight) {
        this.weight = weight
    }

    /**
     * Add an immediate consequence.
     */
    fun immediate(init: ConsequenceDSL.() -> Unit) {
        val consequenceDSL = ConsequenceDSL()
        consequenceDSL.init()
        val consequence = consequenceDSL.build(0)
        consequences.add(consequence)
    }

    /**
     * Add a delayed consequence.
     */
    fun delayed(turns: Int, init: ConsequenceDSL.() -> Unit) {
        val consequenceDSL = ConsequenceDSL()
        consequenceDSL.init()
        val consequence = consequenceDSL.build(turns)
        consequences.add(consequence)
    }

    /**
     * Add a chain reaction consequence.
     */
    fun chainReaction(init: ConsequenceDSL.() -> Unit) {
        val consequenceDSL = ConsequenceDSL()
        consequenceDSL.type = ConsequenceType.CHAIN_REACTION
        consequenceDSL.init()
        val consequence = consequenceDSL.build(0)
        consequences.add(consequence)
    }

    /**
     * Add a tag to the choice.
     */
    fun tag(tag: String) {
        tags.add(tag)
    }

    /**
     * Add metadata to the choice.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Build the choice.
     */
    fun build(): NarrativeChoice {
        if (!::nextBeatId.isInitialized) {
            throw IllegalStateException("Choice must have a next beat ID")
        }

        val choice = NarrativeChoice(
            id = id,
            text = text,
            nextBeatId = nextBeatId,
            condition = condition,
            weight = weight
        )

        // Add consequences
        consequences.forEach { choice.addConsequence(it) }

        // Add tags
        tags.forEach { choice.addTag(it) }

        // Add metadata
        metadata.forEach { (key, value) -> choice.addMetadata(key, value) }

        return choice
    }
}

/**
 * DSL for creating consequences.
 */
class ConsequenceDSL {
    var type: ConsequenceType = ConsequenceType.ATTRIBUTE
    var target: String = ""
    var value: String = ""
    var condition: String? = null
    private val metadata = mutableMapOf<String, String>()

    /**
     * Set the type of the consequence.
     */
    fun type(type: ConsequenceType) {
        this.type = type
    }

    /**
     * Set the target of the consequence.
     */
    fun target(target: String) {
        this.target = target
    }

    /**
     * Set the value of the consequence.
     */
    fun value(value: String) {
        this.value = value
    }

    /**
     * Set the condition for the consequence.
     */
    fun condition(condition: String) {
        this.condition = condition
    }

    /**
     * Add metadata to the consequence.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Build the consequence.
     */
    fun build(delay: Int): Consequence {
        val consequence = Consequence(
            id = UUID.randomUUID().toString(),
            type = type,
            target = target,
            value = value,
            delay = delay,
            condition = condition
        )

        // Add metadata
        metadata.forEach { (key, value) -> consequence.addMetadata(key, value) }

        return consequence
    }
}

/**
 * DSL for creating story arcs.
 */
class ArcDSL(private val id: String) {
    private val title = LocalizedText()
    private val description = LocalizedText()
    private var dependency: String? = null
    private val exclusiveWith = mutableSetOf<String>()
    private val unlocks = mutableSetOf<String>()
    private lateinit var startBeatId: String
    private val endBeatIds = mutableSetOf<String>()
    private val metadata = mutableMapOf<String, String>()

    /**
     * Set the title of the arc.
     */
    fun title(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(title)
    }

    /**
     * Set the description of the arc.
     */
    fun description(init: LocalizedTextDSL.() -> Unit) {
        val localizedTextDSL = LocalizedTextDSL()
        localizedTextDSL.init()
        localizedTextDSL.applyTo(description)
    }

    /**
     * Set the dependency of the arc.
     */
    fun dependency(dependency: String?) {
        this.dependency = dependency
    }

    /**
     * Add an arc that this arc is exclusive with.
     */
    fun exclusiveWith(arcId: String) {
        exclusiveWith.add(arcId)
    }

    /**
     * Add an arc that this arc unlocks.
     */
    fun unlocks(arcId: String) {
        unlocks.add(arcId)
    }

    /**
     * Set the start beat ID of the arc.
     */
    fun setStartBeatId(beatId: String) {
        startBeatId = beatId
    }

    /**
     * Add an end beat ID to the arc.
     */
    fun endBeatId(beatId: String) {
        endBeatIds.add(beatId)
    }

    /**
     * Add metadata to the arc.
     */
    fun metadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Build the story arc.
     */
    fun build(): StoryArc {
        if (!::startBeatId.isInitialized) {
            throw IllegalStateException("Arc must have a start beat ID")
        }

        val arc = StoryArc(
            id = id,
            title = title,
            description = description,
            dependency = dependency,
            exclusiveWith = exclusiveWith,
            unlocks = unlocks,
            startBeatId = startBeatId,
            endBeatIds = endBeatIds
        )

        // Add metadata
        metadata.forEach { (key, value) -> arc.addMetadata(key, value) }

        return arc
    }
}

/**
 * Create a story using the DSL.
 */
fun story(id: String, init: StoryDSL.() -> Unit): Story {
    val storyDSL = StoryDSL()
    storyDSL.setId(id)
    storyDSL.init()
    return storyDSL.build()
}
