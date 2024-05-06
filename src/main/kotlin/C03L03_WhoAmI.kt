package org.j55

import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient
import org.j55.infrastructure.Message
import org.j55.infrastructure.OpenAiClient

private val logger = KotlinLogging.logger {}

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")
    val aiDevsClient = AiDevsClient(apiKey, 2000, "whoami")

    val systemPrompt =
        """You are playing guess-a-person-name game. You will be provided with some facts about the person. Basing on that you will try to guess person name. 
           |You will strictly keep to following rules:
           |- expect at least two facts to guess.
           |- You must be really sure, your guess is correct.
           |- If you are not sure, respond solely with "IDK".
     """.trimMargin()

    val hints = mutableListOf<String>()
    var gptGuess = "IDK"
    do {
        hints.add(getHint(aiDevsClient))
        val messages = listOf(Message("system", systemPrompt), Message("user", formatHints(hints)))
        gptGuess = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
            .callCompletionApi(messages, 1000)
            .also { println(it) }
            .at("/choices/0/message/content")
            .asText()
    } while (gptGuess == "IDK")

    aiDevsClient.postAnswer(
        AiDevsAnswer(gptGuess)
    ).also { logger.info { it } }
}

private fun getHint(aiDevsClient: AiDevsClient): String = aiDevsClient.getTask()
    .also { logger.info { it.toString() } }
    .at("/hint")
    .asText()!!

private fun formatHints(hints: MutableList<String>) = hints.joinToString("\n- ", "Facts:\n- ")