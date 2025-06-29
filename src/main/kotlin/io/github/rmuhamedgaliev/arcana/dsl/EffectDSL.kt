package io.github.rmuhamedgaliev.arcana.dsl

import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.Consequence
import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ConsequenceType
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import java.util.*

/**
 * DSL for creating effects and consequences.
 * Allows for defining immediate effects, delayed consequences, and chain reactions.
 */
class EffectDSL {
    private val consequences = mutableListOf<Consequence>()

    /**
     * Add an attribute effect.
     */
    fun attribute(name: String, value: Int) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.ATTRIBUTE,
                target = name,
                value = value.toString(),
                delay = 0
            )
        )
    }

    /**
     * Add a relationship effect.
     */
    fun relationship(npcId: String, value: Int) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.RELATIONSHIP,
                target = npcId,
                value = value.toString(),
                delay = 0
            )
        )
    }

    /**
     * Add a faction standing effect.
     */
    fun faction(factionId: String, value: Int) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.FACTION,
                target = factionId,
                value = value.toString(),
                delay = 0
            )
        )
    }

    /**
     * Add an item to the inventory.
     */
    fun addItem(itemId: String, quantity: Int = 1) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.ATTRIBUTE,
                target = "inventory:$itemId",
                value = "+$quantity",
                delay = 0
            )
        )
    }

    /**
     * Remove an item from the inventory.
     */
    fun removeItem(itemId: String, quantity: Int = 1) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.ATTRIBUTE,
                target = "inventory:$itemId",
                value = "-$quantity",
                delay = 0
            )
        )
    }

    /**
     * Trigger an event.
     */
    fun triggerEvent(eventId: String) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.EVENT,
                target = eventId,
                value = "trigger",
                delay = 0
            )
        )
    }

    /**
     * Set a world state value.
     */
    fun setWorldState(key: String, value: String) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.WORLD_STATE,
                target = key,
                value = value,
                delay = 0
            )
        )
    }

    /**
     * Add a delayed consequence.
     */
    fun delayed(turns: Int, init: EffectDSL.() -> Unit) {
        val effectDSL = EffectDSL()
        effectDSL.init()

        effectDSL.consequences.forEach { consequence ->
            consequences.add(
                Consequence(
                    id = UUID.randomUUID().toString(),
                    type = consequence.type,
                    target = consequence.target,
                    value = consequence.value,
                    delay = turns,
                    condition = consequence.condition
                )
            )
        }
    }

    /**
     * Add a conditional effect.
     */
    fun conditional(condition: String, init: EffectDSL.() -> Unit) {
        val effectDSL = EffectDSL()
        effectDSL.init()

        effectDSL.consequences.forEach { consequence ->
            consequences.add(
                Consequence(
                    id = UUID.randomUUID().toString(),
                    type = consequence.type,
                    target = consequence.target,
                    value = consequence.value,
                    delay = consequence.delay,
                    condition = condition
                )
            )
        }
    }

    /**
     * Add a chain reaction.
     */
    fun chainReaction(init: EffectDSL.() -> Unit) {
        val effectDSL = EffectDSL()
        effectDSL.init()

        val chainId = UUID.randomUUID().toString()

        // Add the chain reaction trigger
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.CHAIN_REACTION,
                target = chainId,
                value = "trigger",
                delay = 0
            )
        )

        // Add all the consequences in the chain
        effectDSL.consequences.forEach { consequence ->
            consequences.add(
                Consequence(
                    id = UUID.randomUUID().toString(),
                    type = consequence.type,
                    target = consequence.target,
                    value = consequence.value,
                    delay = consequence.delay,
                    condition = "chain:$chainId"
                )
            )
        }
    }

    /**
     * Add a cumulative effect.
     */
    fun cumulative(target: String, value: Int, maxValue: Int? = null) {
        consequences.add(
            Consequence(
                id = UUID.randomUUID().toString(),
                type = ConsequenceType.CUMULATIVE,
                target = target,
                value = "$value:${maxValue ?: ""}",
                delay = 0
            )
        )
    }

    /**
     * Get all consequences.
     */
    fun getConsequences(): List<Consequence> {
        return consequences.toList()
    }
}

/**
 * Create effects using the DSL.
 */
fun effects(init: EffectDSL.() -> Unit): List<Consequence> {
    val effectDSL = EffectDSL()
    effectDSL.init()
    return effectDSL.getConsequences()
}

/**
 * Apply a consequence to a player.
 */
fun applyConsequence(consequence: Consequence, player: Player, story: Story) {
    // Check if the consequence has a condition
    if (consequence.hasCondition()) {
        val condition = consequence.condition!!
        if (!evaluateCondition(condition, player, story)) {
            return
        }
    }

    when (consequence.type) {
        ConsequenceType.ATTRIBUTE -> {
            val target = consequence.target
            val value = consequence.value

            if (value.startsWith("+")) {
                // Increment attribute
                val increment = value.substring(1).toIntOrNull() ?: 0
                val currentValue = player.getAttribute(target)
                player.setAttribute(target, currentValue + increment)
            } else if (value.startsWith("-")) {
                // Decrement attribute
                val decrement = value.substring(1).toIntOrNull() ?: 0
                val currentValue = player.getAttribute(target)
                player.setAttribute(target, currentValue - decrement)
            } else {
                // Set attribute
                val newValue = value.toIntOrNull() ?: 0
                player.setAttribute(target, newValue)
            }
        }

        ConsequenceType.RELATIONSHIP -> {
            val npcId = consequence.target
            val value = consequence.value.toIntOrNull() ?: 0
            val currentValue = player.getAttribute("relationship:$npcId")
            player.setAttribute("relationship:$npcId", currentValue + value)
        }

        ConsequenceType.FACTION -> {
            val factionId = consequence.target
            val value = consequence.value.toIntOrNull() ?: 0
            val currentValue = player.getAttribute("faction:$factionId")
            player.setAttribute("faction:$factionId", currentValue + value)
        }

        ConsequenceType.WORLD_STATE -> {
            val key = consequence.target
            val value = consequence.value
            story.addMetadata("world_state:$key", value)
        }

        ConsequenceType.EVENT -> {
            val eventId = consequence.target
            // In a real implementation, this would trigger an event in the event system
            // For now, we'll just record that the event was triggered
            player.setProgress("event:$eventId", "triggered")
        }

        ConsequenceType.CHAIN_REACTION -> {
            val chainId = consequence.target
            // In a real implementation, this would trigger a chain reaction in the event system
            // For now, we'll just record that the chain reaction was triggered
            player.setProgress("chain:$chainId", "triggered")
        }

        ConsequenceType.CUMULATIVE -> {
            val target = consequence.target
            val parts = consequence.value.split(":")
            val value = parts[0].toIntOrNull() ?: 0
            val maxValue = parts.getOrNull(1)?.toIntOrNull()

            val currentValue = player.getAttribute(target)
            val newValue = currentValue + value

            // Apply max value if specified
            val finalValue = if (maxValue != null && newValue > maxValue) {
                maxValue
            } else {
                newValue
            }

            player.setAttribute(target, finalValue)
        }
    }
}

/**
 * Apply a list of consequences to a player.
 */
fun applyConsequences(consequences: List<Consequence>, player: Player, story: Story) {
    consequences.forEach { consequence ->
        applyConsequence(consequence, player, story)
    }
}
