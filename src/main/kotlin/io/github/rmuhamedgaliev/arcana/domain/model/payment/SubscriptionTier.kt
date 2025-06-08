package io.github.rmuhamedgaliev.arcana.domain.model.payment

/**
 * Enum representing subscription tiers.
 */
enum class SubscriptionTier(
    val displayName: String,
    val priceUsd: Double,
    val saveSlots: Int,
    val hintsAvailable: Boolean,
    val prioritySupport: Boolean,
    val customThemes: Boolean,
    val exclusiveContent: Boolean
) {
    FREE(
        displayName = "Free",
        priceUsd = 0.0,
        saveSlots = 1,
        hintsAvailable = false,
        prioritySupport = false,
        customThemes = false,
        exclusiveContent = false
    ),
    
    BASIC(
        displayName = "Basic",
        priceUsd = 2.99,
        saveSlots = 3,
        hintsAvailable = true,
        prioritySupport = false,
        customThemes = false,
        exclusiveContent = false
    ),
    
    PREMIUM(
        displayName = "Premium",
        priceUsd = 9.99,
        saveSlots = 10,
        hintsAvailable = true,
        prioritySupport = true,
        customThemes = true,
        exclusiveContent = true
    );
    
    /**
     * Check if this tier has access to a feature that requires a minimum tier.
     *
     * @param minimumTier The minimum tier required
     * @return True if this tier has access, false otherwise
     */
    fun hasAccess(minimumTier: SubscriptionTier): Boolean {
        return this.ordinal >= minimumTier.ordinal
    }
    
    companion object {
        /**
         * Get a subscription tier by its display name.
         *
         * @param displayName The display name
         * @return The subscription tier, or null if not found
         */
        fun fromDisplayName(displayName: String): SubscriptionTier? {
            return values().find { it.displayName.equals(displayName, ignoreCase = true) }
        }
    }
}
