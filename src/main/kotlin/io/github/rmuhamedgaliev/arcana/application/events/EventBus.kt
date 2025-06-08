package io.github.rmuhamedgaliev.arcana.application.events

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Interface for an event bus that allows components to publish and subscribe to events.
 */
interface EventBus {
    /**
     * Publish an event to all subscribers.
     *
     * @param event The event to publish
     */
    fun publish(event: Event)

    /**
     * Subscribe to events of a specific type.
     *
     * @param eventType The type of events to subscribe to
     * @param handler The handler to call when an event of the specified type is published
     */
    fun subscribe(eventType: String, handler: EventHandler)

    /**
     * Unsubscribe from events of a specific type.
     *
     * @param eventType The type of events to unsubscribe from
     * @param handler The handler to unsubscribe
     */
    fun unsubscribe(eventType: String, handler: EventHandler)
}

/**
 * Interface for an event handler.
 */
interface EventHandler {
    /**
     * Handle an event.
     *
     * @param event The event to handle
     */
    fun handle(event: Event)
}

/**
 * Interface for an event.
 */
interface Event {
    /**
     * Get the event type.
     *
     * @return The event type
     */
    fun getType(): String

    /**
     * Get the event timestamp.
     *
     * @return The event timestamp
     */
    fun getTimestamp(): Long
}

/**
 * Simple implementation of the EventBus interface.
 */
class SimpleEventBus : EventBus {
    private val subscribers = ConcurrentHashMap<String, CopyOnWriteArrayList<EventHandler>>()

    override fun publish(event: Event) {
        val eventType = event.getType()
        val handlers = subscribers[eventType]

        if (handlers != null) {
            for (handler in handlers) {
                try {
                    handler.handle(event)
                } catch (e: Exception) {
                    System.err.println("Error handling event: ${e.message}")
                }
            }
        }
    }

    override fun subscribe(eventType: String, handler: EventHandler) {
        val handlers = subscribers.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
        handlers.add(handler)
    }

    override fun unsubscribe(eventType: String, handler: EventHandler) {
        subscribers[eventType]?.remove(handler)
    }
}

/**
 * Abstract base class for events.
 */
abstract class AbstractEvent : Event {
    private val timestamp = System.currentTimeMillis()

    override fun getTimestamp(): Long {
        return timestamp
    }
}
