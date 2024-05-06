package org.j55

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.*

private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")
    val aiDevsClient = AiDevsClient(apiKey, 120000, "gnome")
    val openAiClient = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))

    C04L03Gnome(aiDevsClient, openAiClient)
        .resolve()
}

class C04L03Gnome(val aiDevsClient: AiDevsClient, val openAiClient: OpenAiClient) {

    fun resolve() {
        recognizeHatColor(getUrl(aiDevsClient))
            .let { aiDevsClient.postAnswer(AiDevsAnswer(it)) }
            .also { logger.info { it } }

    }

    private fun recognizeHatColor(imageUrl: String): String {
        val systemPrompt = """Image should contain gnome in a hat. Recognize color of the hat in polish language.
            |If image does not contain gnome return solely ERROR. 
        """.trimMargin()
        val visionMessage = VisionMessage(
            "user",
            listOf(
                VisionTextContent(systemPrompt),
                VisionImageContent(Image((imageUrl)))
            )
        )
        return openAiClient.callVisionApi(listOf(visionMessage), 300)
            .at("/choices/0/message/content")
            .also { logger.info { "Color of hat: $it" } }
            .asText()!!
    }


    private fun getUrl(aiDevsClient: AiDevsClient): String = aiDevsClient.getTask()
        .also { logger.info { it.toString() } }
        .at("/url")
        .asText()!!
}