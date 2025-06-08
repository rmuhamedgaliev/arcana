package io.github.rmuhamedgaliev.arcana.domain.model.player

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import java.time.Instant
import java.util.UUID

/**
 * Class representing a player in the game.
 * Stores player attributes, progress information, and subscription details.
 */
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
    val attributes: MutableMap<String, Int> = mutableMapOf(),
    val progress: MutableMap<String, String> = mutableMapOf(),
    var subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    var subscriptionExpiresAt: Instant? = null
) {
    /**
     * Set a player attribute.
     *
     * @param key The attribute key
     * @param value The attribute value
     */
    fun setAttribute(key: String, value: Int) {
        attributes[key] = value
    }

    /**
     * Get a player attribute.
     *
     * @param key The attribute key
     * @return The attribute value, or 0 if not set
     */
    fun getAttribute(key: String): Int {
        return attributes.getOrDefault(key, 0)
    }

    /**
     * Check if a player has an attribute.
     *
     * @param key The attribute key
     * @return True if the player has the attribute, false otherwise
     */
    fun hasAttribute(key: String): Boolean {
        return attributes.containsKey(key)
    }

    /**
     * Set a progress value.
     *
     * @param key The progress key
     * @param value The progress value
     */
    fun setProgress(key: String, value: String) {
        progress[key] = value
    }

    /**
     * Get a progress value.
     *
     * @param key The progress key
     * @return The progress value, or null if not set
     */
    fun getProgress(key: String): String? {
        return progress[key]
    }

    /**
     * Check if a player has a progress value.
     *
     * @param key The progress key
     * @return True if the player has the progress value, false otherwise
     */
    fun hasProgress(key: String): Boolean {
        return progress.containsKey(key)
    }

    /**
     * Check if the player's subscription is active.
     *
     * @return True if the subscription is active, false otherwise
     */
    fun hasActiveSubscription(): Boolean {
        return subscriptionTier != SubscriptionTier.FREE && 
               (subscriptionExpiresAt == null || subscriptionExpiresAt!!.isAfter(Instant.now()))
    }

    /**
     * Check if the player has access to a premium feature.
     *
     * @param minimumTier The minimum subscription tier required
     * @return True if the player has access, false otherwise
     */
    fun hasPremiumAccess(minimumTier: SubscriptionTier): Boolean {
        return hasActiveSubscription() && subscriptionTier.ordinal >= minimumTier.ordinal
    }

    /**
     * Get the number of save slots available to the player.
     *
     * @return The number of save slots
     */
    fun getAvailableSaveSlots(): Int {
        return when {
            !hasActiveSubscription() -> 1
            subscriptionTier == SubscriptionTier.BASIC -> 3
            subscriptionTier == SubscriptionTier.PREMIUM -> 10
            else -> 1
        }
    }
}
