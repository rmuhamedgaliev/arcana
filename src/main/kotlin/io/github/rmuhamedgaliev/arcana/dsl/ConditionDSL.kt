package io.github.rmuhamedgaliev.arcana.dsl

import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story

/**
 * DSL for creating complex conditions.
 * Allows for combining multiple conditions with logical operators.
 */
class ConditionDSL {
    private var condition: (Player, Story) -> Boolean = { _, _ -> true }
    
    /**
     * Check if a player has an attribute.
     */
    fun hasAttribute(name: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasAttribute(name)
        }
        return this
    }
    
    /**
     * Check if a player attribute is equal to a value.
     */
    fun attributeEquals(name: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute(name) == value
        }
        return this
    }
    
    /**
     * Check if a player attribute is greater than a value.
     */
    fun attributeGreaterThan(name: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute(name) > value
        }
        return this
    }
    
    /**
     * Check if a player attribute is less than a value.
     */
    fun attributeLessThan(name: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute(name) < value
        }
        return this
    }
    
    /**
     * Check if a player attribute is in a range.
     */
    fun attributeInRange(name: String, range: IntRange): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute(name) in range
        }
        return this
    }
    
    /**
     * Check if a player has visited a beat.
     */
    fun hasVisited(beatId: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasProgress("visited:${story.id}:$beatId")
        }
        return this
    }
    
    /**
     * Check if a player has made a choice.
     */
    fun hasMadeChoice(choiceId: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasProgress("choice:${story.id}:$choiceId")
        }
        return this
    }
    
    /**
     * Check if a player has a relationship with an NPC.
     */
    fun hasRelationship(npcId: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasAttribute("relationship:$npcId")
        }
        return this
    }
    
    /**
     * Check if a player's relationship with an NPC is at least a certain value.
     */
    fun relationshipAtLeast(npcId: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute("relationship:$npcId") >= value
        }
        return this
    }
    
    /**
     * Check if a player's relationship with an NPC is at most a certain value.
     */
    fun relationshipAtMost(npcId: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute("relationship:$npcId") <= value
        }
        return this
    }
    
    /**
     * Check if a player belongs to a faction.
     */
    fun belongsToFaction(factionId: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasProgress("faction:$factionId")
        }
        return this
    }
    
    /**
     * Check if a player has a faction standing of at least a certain value.
     */
    fun factionStandingAtLeast(factionId: String, value: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.getAttribute("faction:$factionId") >= value
        }
        return this
    }
    
    /**
     * Check if a player has an item.
     */
    fun hasItem(itemId: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && player.hasProgress("inventory:$itemId")
        }
        return this
    }
    
    /**
     * Check if a player has a certain amount of an item.
     */
    fun hasItemQuantity(itemId: String, quantity: Int): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && 
            player.hasProgress("inventory:$itemId") && 
            player.getProgress("inventory:$itemId")?.toIntOrNull() ?: 0 >= quantity
        }
        return this
    }
    
    /**
     * Check if a world state attribute is equal to a value.
     */
    fun worldStateEquals(key: String, value: String): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            previousCondition(player, story) && 
            story.getMetadata("world_state:$key") == value
        }
        return this
    }
    
    /**
     * Negate the condition.
     */
    fun not(): ConditionDSL {
        val previousCondition = condition
        condition = { player, story ->
            !previousCondition(player, story)
        }
        return this
    }
    
    /**
     * Combine with another condition using AND.
     */
    infix fun and(other: ConditionDSL): ConditionDSL {
        val previousCondition = condition
        val otherCondition = other.build()
        condition = { player, story ->
            previousCondition(player, story) && otherCondition(player, story)
        }
        return this
    }
    
    /**
     * Combine with another condition using OR.
     */
    infix fun or(other: ConditionDSL): ConditionDSL {
        val previousCondition = condition
        val otherCondition = other.build()
        condition = { player, story ->
            previousCondition(player, story) || otherCondition(player, story)
        }
        return this
    }
    
    /**
     * Build the condition.
     */
    fun build(): (Player, Story) -> Boolean {
        return condition
    }
}

/**
 * Create a condition using the DSL.
 */
fun condition(init: ConditionDSL.() -> Unit): (Player, Story) -> Boolean {
    val conditionDSL = ConditionDSL()
    conditionDSL.init()
    return conditionDSL.build()
}

/**
 * Evaluate a condition string.
 * This is a simple parser for condition strings that can be used in JSON files.
 */
fun evaluateCondition(conditionString: String, player: Player, story: Story): Boolean {
    // Simple condition parser
    return when {
        // Check for attribute conditions
        conditionString.startsWith("attribute:") -> {
            val parts = conditionString.removePrefix("attribute:").split(":")
            if (parts.size < 2) return false
            
            val attributeName = parts[0]
            val operator = parts[1]
            val value = parts.getOrNull(2)?.toIntOrNull() ?: return false
            
            when (operator) {
                "eq" -> player.getAttribute(attributeName) == value
                "gt" -> player.getAttribute(attributeName) > value
                "lt" -> player.getAttribute(attributeName) < value
                "gte" -> player.getAttribute(attributeName) >= value
                "lte" -> player.getAttribute(attributeName) <= value
                else -> false
            }
        }
        
        // Check for relationship conditions
        conditionString.startsWith("relationship:") -> {
            val parts = conditionString.removePrefix("relationship:").split(":")
            if (parts.size < 2) return false
            
            val npcId = parts[0]
            val operator = parts[1]
            val value = parts.getOrNull(2)?.toIntOrNull() ?: return false
            
            when (operator) {
                "eq" -> player.getAttribute("relationship:$npcId") == value
                "gt" -> player.getAttribute("relationship:$npcId") > value
                "lt" -> player.getAttribute("relationship:$npcId") < value
                "gte" -> player.getAttribute("relationship:$npcId") >= value
                "lte" -> player.getAttribute("relationship:$npcId") <= value
                else -> false
            }
        }
        
        // Check for faction conditions
        conditionString.startsWith("faction:") -> {
            val parts = conditionString.removePrefix("faction:").split(":")
            if (parts.size < 2) return false
            
            val factionId = parts[0]
            val operator = parts[1]
            val value = parts.getOrNull(2)?.toIntOrNull() ?: return false
            
            when (operator) {
                "eq" -> player.getAttribute("faction:$factionId") == value
                "gt" -> player.getAttribute("faction:$factionId") > value
                "lt" -> player.getAttribute("faction:$factionId") < value
                "gte" -> player.getAttribute("faction:$factionId") >= value
                "lte" -> player.getAttribute("faction:$factionId") <= value
                "member" -> player.hasProgress("faction:$factionId")
                else -> false
            }
        }
        
        // Check for item conditions
        conditionString.startsWith("item:") -> {
            val parts = conditionString.removePrefix("item:").split(":")
            if (parts.isEmpty()) return false
            
            val itemId = parts[0]
            
            if (parts.size == 1) {
                // Simple item check
                player.hasProgress("inventory:$itemId")
            } else {
                // Item quantity check
                val operator = parts[1]
                val value = parts.getOrNull(2)?.toIntOrNull() ?: return false
                val quantity = player.getProgress("inventory:$itemId")?.toIntOrNull() ?: 0
                
                when (operator) {
                    "eq" -> quantity == value
                    "gt" -> quantity > value
                    "lt" -> quantity < value
                    "gte" -> quantity >= value
                    "lte" -> quantity <= value
                    else -> false
                }
            }
        }
        
        // Check for world state conditions
        conditionString.startsWith("world:") -> {
            val parts = conditionString.removePrefix("world:").split(":")
            if (parts.size < 2) return false
            
            val key = parts[0]
            val value = parts[1]
            
            story.getMetadata("world_state:$key") == value
        }
        
        // Check for visited beat conditions
        conditionString.startsWith("visited:") -> {
            val beatId = conditionString.removePrefix("visited:")
            player.hasProgress("visited:${story.id}:$beatId")
        }
        
        // Check for made choice conditions
        conditionString.startsWith("choice:") -> {
            val choiceId = conditionString.removePrefix("choice:")
            player.hasProgress("choice:${story.id}:$choiceId")
        }
        
        // Default to false for unknown conditions
        else -> false
    }
}
