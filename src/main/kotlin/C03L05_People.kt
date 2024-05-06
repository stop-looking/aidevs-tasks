package org.j55

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.*
import java.io.File

private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")
    val aiDevsClient = AiDevsClient(apiKey, 120000, "people")
    val openAiClient = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
    val question = getQuestion(aiDevsClient)

    extractPersonName(question, openAiClient)
        .also { logger.info { "Extracted person name: $it" } }
        .let { findAnswer(question, loadPersons()[it]!!, openAiClient) }
        .let { aiDevsClient.postAnswer(AiDevsAnswer(it)) }
}

fun findAnswer(question: String, context: String, openAiClient: OpenAiClient): String {
    val systemPrompt = """You will answer given question with following JSON context:
        |$context
        |### Rules:
        |- You will stick strictly to the context
        |- If you don't know the answer, you will answer solely with "IDK"
        |- You will answer in natural language
        |### Examples:
        |Question: Co lubi jeść Marek Czyż?
        |Answer: pizzę
        |Question: Gdzie mieszka Piotr Puszcz?
        |Answer: W Warszawie
    """.trimIndent()

    return openAiClient.callCompletionApi(listOf(systemMessage(systemPrompt), userMessage(question)), 200)
        .at("/choices/0/message/content")
        .asText()
        .takeIf { "IDK" != it }!!
}


fun extractPersonName(question: String, openAiClient: OpenAiClient): String {
    val systemPrompt = """Your task is to extract name and lastname from given input. 
    |### Rules:
    |* You will only respond with extracted name and lastname
    |* If you are unable to recognize name and surname, you will respond only with "IDK"
    |
    |### Examples:
    |Question: co lubi jeść Tomek Bzik?
    |Answer: Tomek Bzik
    |
    |Question: Czy Antoni Kowalski potrafi skakać?
    |Answer: Antoni Kowalski""".trimMargin()

    return openAiClient.callCompletionApi(listOf(systemMessage(systemPrompt), userMessage(question)), 200)
        .at("/choices/0/message/content")
        .asText()
        .takeIf { "IDK" != it }!!
}

fun loadPersons(): Map<String, String> {
    val inputStreamReader = File("input-files/people.json").reader()
    return jsonMapper.readValue<List<JsonNode>>(inputStreamReader).asSequence()
        .map { Pair("${it["imie"].asText()} ${it["nazwisko"].asText()}", it.toString()) }
        .toMap()
}

private fun getQuestion(aiDevsClient: AiDevsClient): String = aiDevsClient.getTask()
    .also { logger.info { it.toString() } }
    .at("/question")
    .asText()!!
