package io.github.rmuhamedgaliev.arcana.domain.model.mechanics

import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import java.util.UUID
import kotlin.random.Random

/**
 * Class representing a skill check in the game.
 * A skill check is a test of a player's attribute against a difficulty value.
 */
open class SkillCheck(
    val id: String = UUID.randomUUID().toString(),
    val attributeName: String,
    val difficulty: Int,
    val bonusModifier: Int = 0,
    val criticalSuccessThreshold: Int = 18,
    val criticalFailureThreshold: Int = 3,
    val successOutcome: SkillCheckOutcome,
    val failureOutcome: SkillCheckOutcome,
    val criticalSuccessOutcome: SkillCheckOutcome? = null,
    val criticalFailureOutcome: SkillCheckOutcome? = null
) {
    /**
     * Perform the skill check.
     *
     * @param player The player performing the skill check
     * @return The outcome of the skill check
     */
    fun perform(player: Player): SkillCheckOutcome {
        val attributeValue = player.getAttribute(attributeName)
        val roll = rollDice()
        val totalRoll = roll + attributeValue + bonusModifier

        return when {
            // Critical success
            roll >= criticalSuccessThreshold && criticalSuccessOutcome != null -> {
                criticalSuccessOutcome
            }
            // Critical failure
            roll <= criticalFailureThreshold && criticalFailureOutcome != null -> {
                criticalFailureOutcome
            }
            // Success
            totalRoll >= difficulty -> {
                successOutcome
            }
            // Failure
            else -> {
                failureOutcome
            }
        }
    }

    /**
     * Roll a 20-sided die.
     *
     * @return A random number between 1 and 20
     */
    protected open fun rollDice(): Int {
        return Random.nextInt(1, 21)
    }
}

/**
 * Class representing the outcome of a skill check.
 */
data class SkillCheckOutcome(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val nextBeatId: String,
    val consequences: List<Consequence> = emptyList(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    /**
     * Add metadata to the outcome.
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    fun addMetadata(key: String, value: String) {
        metadata[key] = value
    }

    /**
     * Get metadata from the outcome.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    fun getMetadata(key: String): String? {
        return metadata[key]
    }
}

/**
 * Builder for creating skill checks.
 */
class SkillCheckBuilder {
    private var attributeName: String = "agility"
    private var difficulty: Int = 10
    private var bonusModifier: Int = 0
    private var criticalSuccessThreshold: Int = 18
    private var criticalFailureThreshold: Int = 3
    private lateinit var successOutcome: SkillCheckOutcome
    private lateinit var failureOutcome: SkillCheckOutcome
    private var criticalSuccessOutcome: SkillCheckOutcome? = null
    private var criticalFailureOutcome: SkillCheckOutcome? = null

    /**
     * Set the attribute to check.
     */
    fun attribute(name: String) {
        attributeName = name
    }

    /**
     * Set the difficulty of the check.
     */
    fun difficulty(value: Int) {
        difficulty = value
    }

    /**
     * Set the bonus modifier for the check.
     */
    fun bonusModifier(value: Int) {
        bonusModifier = value
    }

    /**
     * Set the threshold for critical success.
     */
    fun criticalSuccessThreshold(value: Int) {
        criticalSuccessThreshold = value
    }

    /**
     * Set the threshold for critical failure.
     */
    fun criticalFailureThreshold(value: Int) {
        criticalFailureThreshold = value
    }

    /**
     * Set the outcome for success.
     */
    fun onSuccess(text: String, nextBeatId: String, consequences: List<Consequence> = emptyList()) {
        successOutcome = SkillCheckOutcome(
            text = text,
            nextBeatId = nextBeatId,
            consequences = consequences
        )
    }

    /**
     * Set the outcome for failure.
     */
    fun onFailure(text: String, nextBeatId: String, consequences: List<Consequence> = emptyList()) {
        failureOutcome = SkillCheckOutcome(
            text = text,
            nextBeatId = nextBeatId,
            consequences = consequences
        )
    }

    /**
     * Set the outcome for critical success.
     */
    fun onCriticalSuccess(text: String, nextBeatId: String, consequences: List<Consequence> = emptyList()) {
        criticalSuccessOutcome = SkillCheckOutcome(
            text = text,
            nextBeatId = nextBeatId,
            consequences = consequences
        )
    }

    /**
     * Set the outcome for critical failure.
     */
    fun onCriticalFailure(text: String, nextBeatId: String, consequences: List<Consequence> = emptyList()) {
        criticalFailureOutcome = SkillCheckOutcome(
            text = text,
            nextBeatId = nextBeatId,
            consequences = consequences
        )
    }

    /**
     * Build the skill check.
     */
    fun build(): SkillCheck {
        if (!::successOutcome.isInitialized) {
            throw IllegalStateException("Success outcome must be set")
        }

        if (!::failureOutcome.isInitialized) {
            throw IllegalStateException("Failure outcome must be set")
        }

        return SkillCheck(
            attributeName = attributeName,
            difficulty = difficulty,
            bonusModifier = bonusModifier,
            criticalSuccessThreshold = criticalSuccessThreshold,
            criticalFailureThreshold = criticalFailureThreshold,
            successOutcome = successOutcome,
            failureOutcome = failureOutcome,
            criticalSuccessOutcome = criticalSuccessOutcome,
            criticalFailureOutcome = criticalFailureOutcome
        )
    }
}

/**
 * Create a skill check using the builder.
 */
fun skillCheck(init: SkillCheckBuilder.() -> Unit): SkillCheck {
    val builder = SkillCheckBuilder()
    builder.init()
    return builder.build()
}
