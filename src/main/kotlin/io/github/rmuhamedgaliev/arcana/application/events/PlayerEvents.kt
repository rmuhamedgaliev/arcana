package io.github.rmuhamedgaliev.arcana.application.events

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import java.time.Instant

/**
 * Base class for all player-related events.
 */
abstract class PlayerEvent : AbstractEvent() {
    abstract val playerId: String
    abstract val eventType: String
    
    override fun getType(): String = eventType
}

/**
 * Event fired when a player registers.
 */
data class PlayerRegisteredEvent(
    override val playerId: String,
    val registrationTime: Instant,
    val platform: String, // e.g., "telegram", "web", "mobile"
    override val eventType: String = "PlayerRegisteredEvent"
) : PlayerEvent()

/**
 * Event fired when a player updates their profile.
 */
data class PlayerProfileUpdatedEvent(
    override val playerId: String,
    val updatedFields: Map<String, Any>,
    override val eventType: String = "PlayerProfileUpdatedEvent"
) : PlayerEvent()

/**
 * Event fired when a player's subscription changes.
 */
data class SubscriptionChangedEvent(
    override val playerId: String,
    val oldTier: SubscriptionTier,
    val newTier: SubscriptionTier,
    val effectiveFrom: Instant,
    val expiresAt: Instant?,
    val reason: String, // e.g., "upgrade", "downgrade", "renewal", "expiration"
    override val eventType: String = "SubscriptionChangedEvent"
) : PlayerEvent()

/**
 * Event fired when a player's subscription is about to expire.
 */
data class SubscriptionExpiringEvent(
    override val playerId: String,
    val tier: SubscriptionTier,
    val expiresAt: Instant,
    val daysRemaining: Int,
    override val eventType: String = "SubscriptionExpiringEvent"
) : PlayerEvent()

/**
 * Event fired when a player logs in.
 */
data class PlayerLoggedInEvent(
    override val playerId: String,
    val loginTime: Instant,
    val platform: String, // e.g., "telegram", "web", "mobile"
    val ipAddress: String,
    val userAgent: String,
    override val eventType: String = "PlayerLoggedInEvent"
) : PlayerEvent()

/**
 * Event fired when a player logs out.
 */
data class PlayerLoggedOutEvent(
    override val playerId: String,
    val logoutTime: Instant,
    val sessionDuration: Long, // in milliseconds
    override val eventType: String = "PlayerLoggedOutEvent"
) : PlayerEvent()

/**
 * Event fired when a player is inactive for a long time.
 */
data class PlayerInactiveEvent(
    override val playerId: String,
    val lastActiveTime: Instant,
    val inactiveDays: Int,
    override val eventType: String = "PlayerInactiveEvent"
) : PlayerEvent()

/**
 * Event fired when a player returns after a long period of inactivity.
 */
data class PlayerReturnedEvent(
    override val playerId: String,
    val returnTime: Instant,
    val inactiveDays: Int,
    override val eventType: String = "PlayerReturnedEvent"
) : PlayerEvent()

/**
 * Event fired when a player achieves a milestone.
 */
data class PlayerMilestoneEvent(
    override val playerId: String,
    val milestoneType: String, // e.g., "games_completed", "choices_made", "days_active"
    val milestoneValue: Int,
    val achievedAt: Instant,
    override val eventType: String = "PlayerMilestoneEvent"
) : PlayerEvent()

/**
 * Event fired when a player unlocks an achievement.
 */
data class AchievementUnlockedEvent(
    override val playerId: String,
    val achievementId: String,
    val achievementName: String,
    val unlockedAt: Instant,
    val rarity: String, // e.g., "common", "uncommon", "rare", "legendary"
    override val eventType: String = "AchievementUnlockedEvent"
) : PlayerEvent()
