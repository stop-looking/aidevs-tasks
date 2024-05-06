package org.j55.aidevs

import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EventListenerService(
    val aiDevsClient: AiDevsClient
) {
    val logger: Logger = LoggerFactory.getLogger(EventListenerService::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReadyEvent(event: ApplicationReadyEvent) {
        logger.info("Sending service URL to AiDevs")
        aiDevsClient.postAnswer(AiDevsAnswer("https://nostromo.czadnet.pl/api/conversation/2"))
        logger.info("Service URL submitted to AiDevs")
    }
}