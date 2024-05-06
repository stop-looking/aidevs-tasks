package org.j55

import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.AiDevsAnswer
import org.j55.infrastructure.AiDevsClient

private val logger = KotlinLogging.logger {}

fun main() {
    val aiDevsClient = AiDevsClient(System.getenv("AI_DEVS_API_KEY"), 120000, "whisper")

    val urlPattern = Regex("https.*mp3")
    val mp3Url = aiDevsClient.getTask()
        .at("/msg")
        .textValue()
        .let { urlPattern.find(it)?.value }

    val hints = mutableListOf<String>()

    aiDevsClient.postAnswer(
        AiDevsAnswer("Cześć! Kiedy ostatnio korzystaliście z sztucznej inteligencji, czy zastanawialiście się nad tym, skąd czerpie ona swoją wiedzę? No pewnie, że tak, inaczej nie byłoby was tutaj na szkoleniu. Ale czy przemyśleliście możliwość dostosowania tej wiedzy do waszych własnych, indywidualnych potrzeb?")
    ).also { logger.info { it } }
}
