package org.j55

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient
import org.j55.infrastructure.Message
import org.j55.infrastructure.OpenAiClient

private val logger = KotlinLogging.logger {}

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")
    val aiDevsClient = AiDevsClient(apiKey, 120000, "scraper")

    val taskDescription =
        aiDevsClient.getTask()
            .also { logger.info { it.toString() } }
    val contextUrl = taskDescription.at("/input").asText()
    val message = taskDescription.at("/message").asText()
    val question = taskDescription.at("/question").asText()
    val context = runBlocking { aiDevsClient.get(contextUrl) }


    val systemPrompt = """$message
        |####
        |Article:
        |$context
    """.trimMargin()

    val messages = listOf(Message("system", systemPrompt), Message("user", question))

    val answer = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
        .callCompletionApi(messages, 1000)
        .also { println(it) }
        .at("/choices/0/message/content")
        .asText()

    aiDevsClient.postAnswer(
        AiDevsAnswer(answer)
    ).also { logger.info { it } }
}