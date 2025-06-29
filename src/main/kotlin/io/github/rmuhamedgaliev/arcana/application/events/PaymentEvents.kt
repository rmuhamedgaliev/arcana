package io.github.rmuhamedgaliev.arcana.application.events

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import java.time.Instant
import java.util.*

/**
 * Base class for all payment-related events.
 */
abstract class PaymentEvent : AbstractEvent() {
    abstract val playerId: String
    abstract val paymentId: String
    abstract val eventType: String

    override fun getType(): String = eventType
}

/**
 * Event fired when a payment is initiated.
 */
data class PaymentInitiatedEvent(
    override val playerId: String,
    override val paymentId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String, // e.g., "credit_card", "paypal", "telegram", "crypto"
    val itemType: String, // e.g., "subscription", "premium_story", "hint_pack"
    val itemId: String,
    val initiatedAt: Instant,
    override val eventType: String = "PaymentInitiatedEvent"
) : PaymentEvent()

/**
 * Event fired when a payment is completed successfully.
 */
data class PaymentCompletedEvent(
    override val playerId: String,
    override val paymentId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val itemType: String,
    val itemId: String,
    val completedAt: Instant,
    val transactionId: String,
    val providerReference: String,
    override val eventType: String = "PaymentCompletedEvent"
) : PaymentEvent()

/**
 * Event fired when a payment fails.
 */
data class PaymentFailedEvent(
    override val playerId: String,
    override val paymentId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val itemType: String,
    val itemId: String,
    val failedAt: Instant,
    val errorCode: String,
    val errorMessage: String,
    val retriable: Boolean,
    override val eventType: String = "PaymentFailedEvent"
) : PaymentEvent()

/**
 * Event fired when a subscription is purchased.
 */
data class SubscriptionPurchasedEvent(
    override val playerId: String,
    override val paymentId: String,
    val tier: SubscriptionTier,
    val amount: Double,
    val currency: String,
    val startDate: Instant,
    val endDate: Instant,
    val autoRenew: Boolean,
    val isInitialPurchase: Boolean,
    override val eventType: String = "SubscriptionPurchasedEvent"
) : PaymentEvent()

/**
 * Event fired when a subscription is renewed.
 */
data class SubscriptionRenewedEvent(
    override val playerId: String,
    override val paymentId: String,
    val tier: SubscriptionTier,
    val amount: Double,
    val currency: String,
    val previousEndDate: Instant,
    val newEndDate: Instant,
    val renewedAt: Instant,
    override val eventType: String = "SubscriptionRenewedEvent"
) : PaymentEvent()

/**
 * Event fired when a subscription is cancelled.
 */
data class SubscriptionCancelledEvent(
    override val playerId: String,
    override val paymentId: String,
    val tier: SubscriptionTier,
    val cancelledAt: Instant,
    val effectiveUntil: Instant,
    val reason: String, // e.g., "user_request", "payment_failure", "admin_action"
    val canReactivate: Boolean,
    override val eventType: String = "SubscriptionCancelledEvent"
) : PaymentEvent()

/**
 * Event fired when a refund is requested.
 */
data class RefundRequestedEvent(
    override val playerId: String,
    override val paymentId: String,
    val originalTransactionId: String,
    val amount: Double,
    val currency: String,
    val requestedAt: Instant,
    val reason: String,
    val requestId: String = UUID.randomUUID().toString(),
    override val eventType: String = "RefundRequestedEvent"
) : PaymentEvent()

/**
 * Event fired when a refund is processed.
 */
data class RefundProcessedEvent(
    override val playerId: String,
    override val paymentId: String,
    val originalTransactionId: String,
    val refundTransactionId: String,
    val amount: Double,
    val currency: String,
    val processedAt: Instant,
    val status: String, // e.g., "approved", "partial", "rejected"
    val requestId: String,
    override val eventType: String = "RefundProcessedEvent"
) : PaymentEvent()

/**
 * Event fired when a premium content is purchased.
 */
data class PremiumContentPurchasedEvent(
    override val playerId: String,
    override val paymentId: String,
    val contentType: String, // e.g., "story", "hint_pack", "character_customization"
    val contentId: String,
    val amount: Double,
    val currency: String,
    val purchasedAt: Instant,
    override val eventType: String = "PremiumContentPurchasedEvent"
) : PaymentEvent()

/**
 * Event fired when a payment provider webhook is received.
 */
data class PaymentWebhookReceivedEvent(
    override val playerId: String,
    override val paymentId: String,
    val provider: String, // e.g., "stripe", "paypal", "telegram"
    val webhookEventType: String, // provider-specific event type
    val payload: Map<String, Any>,
    val receivedAt: Instant,
    override val eventType: String = "PaymentWebhookReceivedEvent"
) : PaymentEvent()
