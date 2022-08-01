package de.bund.digitalservice.useid.events

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.function.Consumer

private val WIDGET_SESSION_ID = UUID.randomUUID()

@Tag("test")
internal class EventServiceTest {
    private val eventService: EventService = EventService()
    private val consumer = mockk<Consumer<Event>>()

    @Test
    fun `subscribe and publish happy path`() {
        // Given
        val event = event(WIDGET_SESSION_ID)
        every { consumer.accept(any()) } returns Unit

        // When
        eventService.subscribeConsumer(WIDGET_SESSION_ID, consumer)

        // Then
        assertEquals(1, eventService.numberConsumers())

        // When
        eventService.publish(event)

        // Then
        verify { consumer.accept(event) }
    }

    @Test
    fun `publish throws exception if customer is unkown`() {
        // Given
        val unknownId = UUID.randomUUID()
        val event = event(unknownId)
        every { consumer.accept(any()) } returns Unit

        // When
        eventService.subscribeConsumer(WIDGET_SESSION_ID, consumer)

        // Then
        assertEquals(1, eventService.numberConsumers())
        val exception = assertThrows<ConsumerNotFoundException> {
            // When
            eventService.publish(event)
        }
        assertEquals("No consumer found for widget session with id $unknownId.", exception.message)
        verify(exactly = 0) { consumer.accept(event) }
    }

    private fun event(widgetSessionId: UUID) = Event(widgetSessionId, "some-refresh-address")
}