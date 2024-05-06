package org.j55.aidevs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

@SpringBootApplication
class OwnApiApplication

fun main(args: Array<String>) {
    runApplication<OwnApiApplication>(*args)
}
