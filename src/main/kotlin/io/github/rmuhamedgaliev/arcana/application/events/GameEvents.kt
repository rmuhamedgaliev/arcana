package io.github.rmuhamedgaliev.arcana.application.events

import io.github.rmuhamedgaliev.arcana.domain.model.story.NarrativeChoice
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.domain.model.story.StoryBeat

/**
 * Base class for all game-related events.
 */
abstract class GameEvent : AbstractEvent() {
    abstract val playerId: String
    abstract val storyId: String
    abstract val eventType: String

    override fun getType(): String = eventType
}

/**
 * Event fired when a player starts a game.
 */
data class GameStartedEvent(
    override val playerId: String,
    override val storyId: String,
    val storyTitle: String,
    val initialBeatId: String,
    override val eventType: String = "GameStartedEvent"
) : GameEvent()

/**
 * Event fired when a player makes a choice in a game.
 */
data class ChoiceMadeEvent(
    override val playerId: String,
    override val storyId: String,
    val beatId: String,
    val choiceId: String,
    val nextBeatId: String,
    override val eventType: String = "ChoiceMadeEvent"
) : GameEvent()

/**
 * Event fired when a player reaches a new beat in a game.
 */
data class BeatReachedEvent(
    override val playerId: String,
    override val storyId: String,
    val beatId: String,
    val isEndBeat: Boolean,
    override val eventType: String = "BeatReachedEvent"
) : GameEvent()

/**
 * Event fired when a player completes a game.
 */
data class GameCompletedEvent(
    override val playerId: String,
    override val storyId: String,
    val endingId: String,
    val totalChoices: Int,
    val totalBeatsVisited: Int,
    val playTime: Long, // in milliseconds
    override val eventType: String = "GameCompletedEvent"
) : GameEvent()

/**
 * Event fired when a player unlocks a story arc.
 */
data class ArcUnlockedEvent(
    override val playerId: String,
    override val storyId: String,
    val arcId: String,
    override val eventType: String = "ArcUnlockedEvent"
) : GameEvent()

/**
 * Event fired when a player's attribute changes.
 */
data class AttributeChangedEvent(
    override val playerId: String,
    override val storyId: String,
    val attributeName: String,
    val oldValue: Int,
    val newValue: Int,
    val source: String, // e.g., "choice", "beat", "consequence"
    override val eventType: String = "AttributeChangedEvent"
) : GameEvent()

/**
 * Event fired when a delayed consequence is triggered.
 */
data class DelayedConsequenceTriggeredEvent(
    override val playerId: String,
    override val storyId: String,
    val consequenceId: String,
    val originalChoiceId: String,
    val delayTurns: Int,
    override val eventType: String = "DelayedConsequenceTriggeredEvent"
) : GameEvent()

/**
 * Event fired when a player saves their progress.
 */
data class ProgressSavedEvent(
    override val playerId: String,
    override val storyId: String,
    val currentBeatId: String,
    val saveSlot: Int,
    override val eventType: String = "ProgressSavedEvent"
) : GameEvent()

/**
 * Event fired when a player loads their progress.
 */
data class ProgressLoadedEvent(
    override val playerId: String,
    override val storyId: String,
    val loadedBeatId: String,
    val saveSlot: Int,
    override val eventType: String = "ProgressLoadedEvent"
) : GameEvent()
