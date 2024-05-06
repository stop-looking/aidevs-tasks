package org.j55

import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient

private val logger = KotlinLogging.logger {}

fun main() {
    val aiDevsClient = AiDevsClient(System.getenv("AI_DEVS_API_KEY"), 120000, "functions")
    aiDevsClient.getTask().also { logger.info { it } }
    val systemPrompt =
        """{
  "answer": {
    "name": "addUser",
    "description": "Dodaje nowego użytkownika",
    "parameters": {
      "name": {
        "type": "string",
        "description": "Imię użytkownika"
      },
      "surname": {
        "type": "string",
        "description": "Nazwisko użytkownika"
      },
      "year": {
        "type": "integer",
        "description": "Rok urodzenia użytkownika"
      }
    }
  }
}
     """.trimMargin()



    aiDevsClient.postAnswer(
        systemPrompt
    ).also { logger.info { it } }
}
