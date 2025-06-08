# Game Creation Guide

## Quick Start
1. Create story using Kotlin DSL
2. Define characters and relationships
3. Set up world state
4. Configure multiple endings
5. Test your story
6. Deploy to production

## Your First Game (5 minutes)
Creating your first game with Arcana is simple. Follow these steps to create a basic adventure:

1. **Create a new Kotlin file** in the `examples` directory with a descriptive name for your game.
2. **Define your story structure** using the Kotlin DSL:

```kotlin
story("my_first_adventure") {
    title {
        en = "My First Adventure"
        ru = "Моё первое приключение"
    }
    
    description {
        en = "A simple adventure to learn the Arcana engine"
        ru = "Простое приключение для изучения движка Arcana"
    }
    
    // Define your first story beat
    beat("start") {
        text {
            en = "You wake up in a mysterious forest. There are paths leading north and east."
            ru = "Вы просыпаетесь в таинственном лесу. Есть тропинки, ведущие на север и восток."
        }
        
        choice("go_north") {
            text {
                en = "Go north"
                ru = "Идти на север"
            }
            goto("north_clearing")
        }
        
        choice("go_east") {
            text {
                en = "Go east"
                ru = "Идти на восток"
            }
            goto("east_river")
        }
    }
    
    beat("north_clearing") {
        text {
            en = "You arrive at a clearing. There's a small cottage."
            ru = "Вы приходите на поляну. Здесь стоит маленький домик."
        }
        
        choice("enter_cottage") {
            text {
                en = "Enter the cottage"
                ru = "Войти в домик"
            }
            goto("inside_cottage")
        }
        
        choice("return") {
            text {
                en = "Return to the forest"
                ru = "Вернуться в лес"
            }
            goto("start")
        }
    }
    
    beat("east_river") {
        text {
            en = "You come to a river. The water is flowing rapidly."
            ru = "Вы подходите к реке. Вода быстро течет."
        }
        
        choice("cross_river") {
            text {
                en = "Try to cross the river"
                ru = "Попытаться пересечь реку"
            }
            goto("other_side")
        }
        
        choice("return") {
            text {
                en = "Return to the forest"
                ru = "Вернуться в лес"
            }
            goto("start")
        }
    }
    
    // Add more beats to complete your story
}
```

3. **Test your game** by running it in the Arcana engine.
4. **Expand your story** by adding more beats, choices, and consequences.

## Kotlin DSL Reference

### Story Structure

The Arcana engine uses a Kotlin DSL (Domain Specific Language) to define interactive stories. Here's a complete reference of all available constructs:

#### Story Definition

```kotlin
story("unique_story_id") {
    // Story content
}
```

#### Basic Story Properties

```kotlin
title {
    en = "English Title"
    ru = "Russian Title"
}

description {
    en = "English description of the story"
    ru = "Russian description of the story"
}

initialAttributes {
    "strength" to 10
    "intelligence" to 8
    "charisma" to 12
}

requiredSubscriptionTier(SubscriptionTier.PREMIUM)

tag("fantasy")
tag("adventure")

metadata("author", "Your Name")
metadata("version", "1.0")
```

#### Story Beats

```kotlin
beat("unique_beat_id") {
    text {
        en = "English text describing this story beat"
        ru = "Russian text describing this story beat"
    }
    
    // Define choices for this beat
    choice("choice_id") {
        // Choice definition
    }
    
    isEndBeat(true) // Mark as an ending
    
    attribute("mood", "dark")
    tag("important")
    metadata("created", "2023-06-01")
}
```

#### Choices

```kotlin
choice("unique_choice_id") {
    text {
        en = "English text for this choice"
        ru = "Russian text for this choice"
    }
    
    goto("next_beat_id") // Where this choice leads
    
    condition("intelligence >= 10") // Only show if condition is met
    
    weight(ChoiceWeight.RARE) // How likely NPCs are to choose this
    
    immediate {
        // Consequences that happen right away
    }
    
    delayed(turns = 3) {
        // Consequences that happen after 3 turns
    }
    
    chainReaction {
        // Consequences that trigger other consequences
    }
    
    tag("risky")
    metadata("importance", "high")
}
```

#### Consequences

```kotlin
immediate {
    type(ConsequenceType.ATTRIBUTE)
    target("gold")
    value("+50")
    condition("charisma > 15")
    metadata("source", "treasure")
}
```

#### Story Arcs

```kotlin
arc("main_quest") {
    title {
        en = "The Main Quest"
        ru = "Основной квест"
    }
    
    description {
        en = "The primary storyline"
        ru = "Основная сюжетная линия"
    }
    
    dependency(null) // No dependencies, always available
    exclusiveWith("side_quest") // Cannot be active at same time as side_quest
    unlocks("epilogue") // Completing this arc unlocks the epilogue
    
    setStartBeatId("quest_start")
    endBeatId("quest_complete")
    
    metadata("difficulty", "medium")
}
```

### Best Practices for Performance

1. **Minimize condition complexity**: Complex conditions can slow down game performance. Keep conditions simple and focused.

2. **Use tags efficiently**: Tags are a lightweight way to mark and filter content. Use them instead of complex metadata queries when possible.

3. **Batch consequences**: Group related consequences together rather than creating many small ones.

4. **Limit delayed consequences**: While powerful, too many delayed consequences can impact performance. Use them judiciously.

5. **Structure your story hierarchically**: Use arcs to organize your story into logical segments. This improves both performance and maintainability.

### Common Patterns and Anti-Patterns

#### Patterns (Do These)

1. **Hub and Spoke**: Create a central "hub" beat that branches out to different "spoke" beats, each representing a different quest or activity.

2. **Gated Progress**: Use conditions to gate progress based on player attributes or story flags.

3. **Foreshadowing**: Use delayed consequences to set up future events based on current choices.

4. **Incremental Revelation**: Gradually reveal story elements through multiple playthroughs.

5. **State Tracking**: Use metadata to track complex state that affects the story.

#### Anti-Patterns (Avoid These)

1. **Dead Ends**: Beats with no choices or only choices that lead to failure without warning.

2. **Excessive Linearity**: Stories where player choices don't meaningfully affect outcomes.

3. **Attribute Inflation**: Rapidly increasing player attributes making later challenges trivial.

4. **Condition Explosion**: Overly complex conditions that are hard to maintain and debug.

5. **Inconsistent Consequences**: Actions that have wildly different consequences in similar situations without clear reason.

## Advanced Features Guide

### Complex Branching

Creating complex branching narratives allows for rich, non-linear storytelling:

```kotlin
// Branching based on player attributes
choice("negotiate") {
    text {
        en = "Try to negotiate"
        ru = "Попытаться договориться"
    }
    condition("charisma >= 15")
    goto("successful_negotiation")
}

// Branching based on previous choices
choice("use_key") {
    text {
        en = "Use the key you found earlier"
        ru = "Использовать ключ, который вы нашли ранее"
    }
    condition("hasFlag('found_key')")
    goto("unlock_door")
}

// Branching based on world state
choice("cross_bridge") {
    text {
        en = "Cross the bridge"
        ru = "Перейти мост"
    }
    condition("worldState.environment.bridgeIntact == true")
    goto("other_side")
}
```

### Character Relationships

The Arcana engine supports complex character relationships and NPC interactions:

```kotlin
// Define characters
characters {
    npc("elder_wizard") {
        name { 
            en = "Eldrin the Wise"
            ru = "Элдрин Мудрый" 
        }
        faction = "mages_guild"
        personality = listOf("wise", "cautious", "helpful")
        initialRelationship = 20
    }
}

// Modify relationships through choices
choice("help_wizard") {
    text {
        en = "Offer to help the wizard"
        ru = "Предложить помощь волшебнику"
    }
    immediate {
        modifyRelationship("elder_wizard", +15)
    }
    goto("wizard_quest")
}

// Choices affected by relationships
choice("ask_for_spell") {
    text {
        en = "Ask for a powerful spell"
        ru = "Попросить мощное заклинание"
    }
    condition("relationship('elder_wizard') >= 50")
    goto("receive_spell")
}
```

### World State Management

Manage global events and world state to create a dynamic, responsive game world:

```kotlin
// Initialize world state
worldState {
    political {
        kingdomStability = 70
        warThreat = 30
        corruption = 45
    }
    environment {
        weather = "sunny"
        season = "summer"
        bridgeIntact = true
    }
    economic {
        marketPrices = "normal"
        tradingRoutes = "open"
    }
}

// Choices that affect world state
choice("expose_corruption") {
    text {
        en = "Expose the corruption in the court"
        ru = "Разоблачить коррупцию при дворе"
    }
    immediate {
        worldState.political.corruption -= 20
        worldState.political.kingdomStability -= 15
    }
    goto("political_turmoil")
}

// World events triggered by state changes
beat("market_scene") {
    text {
        en = """
            You enter the marketplace.
            {if worldState.economic.marketPrices == "inflated"}
            Prices are unusually high today, and people seem distressed.
            {else}
            The market is bustling with activity and fair prices.
            {endif}
        """.trimIndent()
        ru = """
            Вы входите на рынок.
            {if worldState.economic.marketPrices == "inflated"}
            Цены сегодня необычно высоки, и люди кажутся обеспокоенными.
            {else}
            Рынок кипит активностью и справедливыми ценами.
            {endif}
        """.trimIndent()
    }
}
```

### Consequence Chains

Create complex cause-and-effect relationships with different types of consequences:

#### Immediate Consequences

```kotlin
choice("steal_artifact") {
    text {
        en = "Steal the artifact when no one is looking"
        ru = "Украсть артефакт, когда никто не смотрит"
    }
    immediate {
        // Add item to inventory
        addItem("ancient_artifact")
        // Decrease reputation
        reputation -= 10
        // Set a flag
        setFlag("stole_artifact")
    }
    goto("escape_temple")
}
```

#### Delayed Consequences

```kotlin
choice("insult_noble") {
    text {
        en = "Insult the noble"
        ru = "Оскорбить дворянина"
    }
    immediate {
        // Immediate effect
        modifyRelationship("noble_family", -20)
    }
    delayed(turns = 3) {
        // After 3 turns
        setFlag("assassins_sent")
        triggerEvent("assassination_attempt")
    }
    goto("tavern_exit")
}
```

#### Cascade Effects

```kotlin
choice("start_fire") {
    text {
        en = "Start a fire as a distraction"
        ru = "Устроить пожар для отвлечения внимания"
    }
    immediate {
        setFlag("fire_started")
    }
    chainReaction {
        // This will trigger other events
        worldState.environment.cityDistrict = "burning"
        triggerEvent("city_panic")
        // Which might trigger even more events
    }
    goto("escape_route")
}
```

### Multiple Endings

Design multiple endings based on player choices, attributes, and world state:

```kotlin
// Define endings
endings {
    ending("hero_ending") {
        requirements {
            reputation >= 75 &&
            hasFlag("defeated_villain") &&
            relationship("kingdom") >= 50
        }
        
        rarity = EndingRarity.RARE
        category = EndingCategory.HEROIC
        
        text {
            en = "You are celebrated as a hero throughout the kingdom. Songs are sung of your deeds for generations to come."
            ru = "Вас прославляют как героя по всему королевству. Песни о ваших подвигах поют на протяжении поколений."
        }
        
        unlocks {
            newGamePlus = true
            characterBonus = "hero_legacy"
            achievementId = "true_hero"
        }
    }
    
    ending("villain_ending") {
        requirements {
            reputation <= 25 &&
            hasFlag("betrayed_kingdom") &&
            gold >= 10000
        }
        
        rarity = EndingRarity.UNCOMMON
        category = EndingCategory.VILLAINOUS
        
        text {
            en = "You retreat to your fortress of solitude, wealthy beyond measure but forever marked as an enemy of the kingdom."
            ru = "Вы уединяетесь в своей крепости, невероятно богаты, но навсегда отмечены как враг королевства."
        }
    }
    
    // Define more endings for different play styles and choices
}
```

## Monetization Integration

### Premium Content Marking

Mark premium content that requires a subscription:

```kotlin
// Premium story
story("epic_adventure") {
    requiredSubscriptionTier(SubscriptionTier.PREMIUM)
    // Story content
}

// Individual premium beat
beat("special_encounter") {
    metadata("subscription", "PREMIUM")
    // Beat content
}

// Premium choice
choice("rare_option") {
    requiresSubscription = "PREMIUM"
    // Choice content
}
```

### Subscription Gating

Control access to content based on subscription level:

```kotlin
// Basic subscription check
choice("enter_premium_area") {
    text {
        en = "Enter the mysterious cave [PREMIUM]"
        ru = "Войти в таинственную пещеру [ПРЕМИУМ]"
    }
    requiresSubscription = "PREMIUM"
    goto("premium_cave")
}

// Tiered content
beat("treasure_room") {
    text {
        en = """
            You discover a treasure room filled with gold and jewels.
            {if subscription == "PREMIUM"}
            In the center, you notice a legendary artifact glowing with power.
            {endif}
        """.trimIndent()
    }
    
    // Basic choice for all users
    choice("take_gold") {
        text {
            en = "Take the gold and jewels"
            ru = "Взять золото и драгоценности"
        }
        immediate {
            gold += 500
        }
        goto("exit_dungeon")
    }
    
    // Premium choice
    choice("take_artifact") {
        text {
            en = "Take the legendary artifact [PREMIUM]"
            ru = "Взять легендарный артефакт [ПРЕМИУМ]"
        }
        requiresSubscription = "PREMIUM"
        immediate {
            addItem("legendary_artifact")
            power += 50
        }
        goto("artifact_power")
    }
}
```

### Premium Choices and Storylines

Create exclusive content for premium subscribers:

```kotlin
// Premium story arc
arc("dragon_quest") {
    title {
        en = "The Dragon's Challenge"
        ru = "Испытание дракона"
    }
    metadata("subscription", "PREMIUM")
    // Arc content
}

// Premium ending
endings {
    ending("legendary_hero") {
        requirements {
            hasSubscription("PREMIUM") &&
            reputation >= 90 &&
            hasItem("excalibur")
        }
        
        rarity = EndingRarity.LEGENDARY
        
        text {
            en = "You become a legendary hero whose name echoes through eternity."
            ru = "Вы становитесь легендарным героем, чье имя звучит сквозь вечность."
        }
    }
}
```

### Analytics Integration

Track player choices and progress for analytics:

```kotlin
choice("critical_decision") {
    text {
        en = "Make the critical decision"
        ru = "Принять критическое решение"
    }
    
    metadata("analytics", "true")
    metadata("decision_point", "main_plot_climax")
    
    goto("decision_outcome")
}

beat("game_milestone") {
    text {
        en = "You've reached an important milestone in your journey."
        ru = "Вы достигли важной вехи в своем путешествии."
    }
    
    metadata("analytics", "true")
    metadata("milestone", "mid_game")
    metadata("player_stats", "track")
}
```

## Testing Your Game

### Testing All Paths

Ensure all paths in your story are reachable and make sense:

1. **Create a test plan** that covers all beats and choices.
2. **Use debug flags** to quickly navigate to specific parts of your story.
3. **Test edge cases** where player attributes are at extremes.
4. **Verify all endings** are achievable through normal gameplay.

Example debug setup:

```kotlin
// Add debug options in development mode
beat("crossroads") {
    // Normal content
    
    // Debug choices (can be removed for production)
    if (isDebugMode) {
        choice("debug_goto_dragon") {
            text { en = "[DEBUG] Go to dragon encounter" }
            goto("dragon_encounter")
        }
        
        choice("debug_max_stats") {
            text { en = "[DEBUG] Set max stats" }
            immediate {
                strength = 100
                intelligence = 100
                charisma = 100
                gold = 10000
            }
            goto("crossroads")
        }
    }
}
```

### Validation Tools

Use built-in validation tools to check your story for issues:

```kotlin
// Run from command line
./gradlew validateStory --story=my_adventure

// Or programmatically
val validator = StoryValidator()
val results = validator.validate(myStory)
results.errors.forEach { println(it) }
```

Common validation checks:
- Missing beats referenced by choices
- Unreachable beats
- Circular references
- Beats with no choices
- Unachievable conditions

### Performance Considerations

Optimize your story for performance:

1. **Limit the number of active delayed consequences** at any time.
2. **Use simple conditions** rather than complex nested ones.
3. **Avoid excessive string interpolation** in beat text.
4. **Break very large stories** into multiple connected stories.
5. **Use tags for filtering** instead of complex queries.

### Bug Reporting

When you encounter bugs:

1. **Document the exact sequence** of choices that led to the bug.
2. **Note the game state** at the time of the bug (attributes, flags, etc.).
3. **Take screenshots** if relevant.
4. **Report the issue** with all relevant details.

Example bug report format:

```
Bug: Gold not increasing after treasure choice

Steps to reproduce:
1. Start "Forest Adventure"
2. Choose "Go north" at the starting beat
3. Choose "Enter cave" at the clearing
4. Choose "Open treasure chest"
5. Expected: Gold increases by 50
6. Actual: Gold remains unchanged

Game state:
- Story ID: forest_adventure
- Beat ID: cave_interior
- Player attributes: strength=15, intelligence=12
- Flags set: found_map=true, defeated_goblin=true
```

## JSON Format Legacy Support

### Converting Existing JSON Games

If you have existing games in JSON format, you can convert them to the Kotlin DSL:

1. **Use the conversion tool**:

```bash
./gradlew convertJsonToKotlin --input=games/my_game.json --output=src/main/kotlin/games/MyGame.kt
```

2. **Manual conversion example**:

Original JSON:
```json
{
  "id": "simple_adventure",
  "title": {
    "en": "Simple Adventure",
    "ru": "Простое приключение"
  },
  "scenes": [
    {
      "id": "start",
      "text": {
        "en": "You are at a crossroads.",
        "ru": "Вы на перекрестке."
      },
      "options": [
        {
          "id": "go_north",
          "text": {
            "en": "Go north",
            "ru": "Идти на север"
          },
          "next": "north_scene"
        },
        {
          "id": "go_south",
          "text": {
            "en": "Go south",
            "ru": "Идти на юг"
          },
          "next": "south_scene"
        }
      ]
    }
  ]
}
```

Converted Kotlin DSL:
```kotlin
story("simple_adventure") {
    title {
        en = "Simple Adventure"
        ru = "Простое приключение"
    }
    
    beat("start") {
        text {
            en = "You are at a crossroads."
            ru = "Вы на перекрестке."
        }
        
        choice("go_north") {
            text {
                en = "Go north"
                ru = "Идти на север"
            }
            goto("north_scene")
        }
        
        choice("go_south") {
            text {
                en = "Go south"
                ru = "Идти на юг"
            }
            goto("south_scene")
        }
    }
    
    // Add other scenes
}
```

### Migration Tools

Use the provided migration tools to help with the conversion process:

```bash
# Analyze JSON game structure
./gradlew analyzeJsonGame --input=games/my_game.json

# Validate JSON game
./gradlew validateJsonGame --input=games/my_game.json

# Convert all JSON games in a directory
./gradlew batchConvertJsonGames --input=games/ --output=src/main/kotlin/games/
```

### Compatibility Guidelines

When converting from JSON to Kotlin DSL:

1. **Preserve IDs** to maintain save compatibility.
2. **Map JSON properties** to their DSL equivalents:
   - `scenes` → `beat`
   - `options` → `choice`
   - `next` → `goto`
3. **Add new features** that weren't possible in JSON:
   - Conditions
   - Consequences
   - Character relationships
   - World state
4. **Test thoroughly** after conversion to ensure the story plays the same way.
5. **Gradually enhance** with new DSL features rather than rewriting everything at once.
