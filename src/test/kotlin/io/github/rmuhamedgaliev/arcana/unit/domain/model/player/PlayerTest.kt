package io.github.rmuhamedgaliev.arcana.unit.domain.model.player

import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.player.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlayerTest {

    private lateinit var player: Player

    @BeforeEach
    fun setUp() {
        player = Player(
            id = "player1",
            createdAt = Instant.now()
        )
    }

    @Test
    fun `should create player with correct properties`() {
        // Given
        val id = "player1"

        // Then
        assertEquals(id, player.id)
        assertNotNull(player.createdAt)
        assertTrue(player.attributes.isEmpty())
        assertTrue(player.progress.isEmpty())
        assertEquals(SubscriptionTier.FREE, player.subscriptionTier)
        assertEquals(null, player.subscriptionExpiresAt)
    }

    @Test
    fun `should set and get attributes`() {
        // When
        player.setAttribute("strength", 10)
        player.setAttribute("intelligence", 15)

        // Then
        assertEquals(10, player.getAttribute("strength"))
        assertEquals(15, player.getAttribute("intelligence"))
        assertEquals(0, player.getAttribute("nonexistent"))
    }

    @Test
    fun `should check if player has attribute`() {
        // Given
        player.setAttribute("strength", 10)

        // Then
        assertTrue(player.hasAttribute("strength"))
        assertFalse(player.hasAttribute("nonexistent"))
    }

    @Test
    fun `should set and get progress`() {
        // When
        player.setProgress("main_quest", "completed")
        player.setProgress("side_quest", "in_progress")

        // Then
        assertEquals("completed", player.getProgress("main_quest"))
        assertEquals("in_progress", player.getProgress("side_quest"))
        assertEquals(null, player.getProgress("nonexistent"))
    }

    @Test
    fun `should check if player has progress`() {
        // Given
        player.setProgress("main_quest", "completed")

        // Then
        assertTrue(player.hasProgress("main_quest"))
        assertFalse(player.hasProgress("nonexistent"))
    }

    @Test
    fun `should check if subscription is active`() {
        // Given
        val now = Instant.now()
        val futureTime = now.plus(30, ChronoUnit.DAYS)
        val pastTime = now.minus(30, ChronoUnit.DAYS)

        // When - Free tier
        player.subscriptionTier = SubscriptionTier.FREE
        player.subscriptionExpiresAt = futureTime

        // Then
        assertFalse(player.hasActiveSubscription())

        // When - Basic tier with future expiration
        player.subscriptionTier = SubscriptionTier.BASIC
        player.subscriptionExpiresAt = futureTime

        // Then
        assertTrue(player.hasActiveSubscription())

        // When - Basic tier with past expiration
        player.subscriptionTier = SubscriptionTier.BASIC
        player.subscriptionExpiresAt = pastTime

        // Then
        assertFalse(player.hasActiveSubscription())

        // When - Premium tier with null expiration (lifetime)
        player.subscriptionTier = SubscriptionTier.PREMIUM
        player.subscriptionExpiresAt = null

        // Then
        assertTrue(player.hasActiveSubscription())
    }

    @Test
    fun `should check if player has premium access`() {
        // Given
        val futureTime = Instant.now().plus(30, ChronoUnit.DAYS)

        // When - Free tier
        player.subscriptionTier = SubscriptionTier.FREE
        player.subscriptionExpiresAt = futureTime

        // Then
        assertFalse(player.hasPremiumAccess(SubscriptionTier.BASIC))
        assertFalse(player.hasPremiumAccess(SubscriptionTier.PREMIUM))

        // When - Basic tier
        player.subscriptionTier = SubscriptionTier.BASIC
        player.subscriptionExpiresAt = futureTime

        // Then
        assertTrue(player.hasPremiumAccess(SubscriptionTier.BASIC))
        assertFalse(player.hasPremiumAccess(SubscriptionTier.PREMIUM))

        // When - Premium tier
        player.subscriptionTier = SubscriptionTier.PREMIUM
        player.subscriptionExpiresAt = futureTime

        // Then
        assertTrue(player.hasPremiumAccess(SubscriptionTier.BASIC))
        assertTrue(player.hasPremiumAccess(SubscriptionTier.PREMIUM))
    }

    @Test
    fun `should get correct number of available save slots`() {
        // Given
        val futureTime = Instant.now().plus(30, ChronoUnit.DAYS)
        val pastTime = Instant.now().minus(30, ChronoUnit.DAYS)

        // When - Free tier
        player.subscriptionTier = SubscriptionTier.FREE

        // Then
        assertEquals(1, player.getAvailableSaveSlots())

        // When - Basic tier with active subscription
        player.subscriptionTier = SubscriptionTier.BASIC
        player.subscriptionExpiresAt = futureTime

        // Then
        assertEquals(3, player.getAvailableSaveSlots())

        // When - Basic tier with expired subscription
        player.subscriptionTier = SubscriptionTier.BASIC
        player.subscriptionExpiresAt = pastTime

        // Then
        assertEquals(1, player.getAvailableSaveSlots())

        // When - Premium tier with active subscription
        player.subscriptionTier = SubscriptionTier.PREMIUM
        player.subscriptionExpiresAt = futureTime

        // Then
        assertEquals(10, player.getAvailableSaveSlots())
    }
}
