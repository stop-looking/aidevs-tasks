package org.j55

import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient
import org.j55.infrastructure.EmbeddingModel
import org.j55.infrastructure.OpenAiClient

private val logger = KotlinLogging.logger {}

fun main() {
    val aiDevsClient = AiDevsClient(System.getenv("AI_DEVS_API_KEY"), 120000, "embedding")

    val input = "Hawaiian pizza"

    val embedding = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
        .callEmbeddingApi(input, EmbeddingModel.ADA)
        .also { logger.info { "Response: $it" } }
        .at("/data/0/embedding")

    aiDevsClient.postAnswer(
        AiDevsAnswer(embedding)
    )
}