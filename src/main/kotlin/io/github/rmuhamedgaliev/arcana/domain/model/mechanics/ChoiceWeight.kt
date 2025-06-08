package io.github.rmuhamedgaliev.arcana.domain.model.mechanics

/**
 * Enum representing the weight of a choice.
 * This affects how prominently the choice is displayed to the player.
 */
enum class ChoiceWeight(val displayFactor: Double) {
    /**
     * Barely visible choice, displayed with low prominence.
     */
    BARELY_VISIBLE(0.5),
    
    /**
     * Normal choice, displayed with standard prominence.
     */
    NORMAL(1.0),
    
    /**
     * Prominent choice, displayed with high prominence.
     */
    PROMINENT(1.5),
    
    /**
     * Very prominent choice, displayed with very high prominence.
     */
    VERY_PROMINENT(2.0);
    
    companion object {
        /**
         * Get a choice weight by its name.
         *
         * @param name The name of the choice weight
         * @return The choice weight, or NORMAL if not found
         */
        fun fromName(name: String): ChoiceWeight {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: NORMAL
        }
    }
}
