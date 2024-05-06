package org.j55

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.j55.infrastructure.*

private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")

    C04L02Tools(
        AiDevsClient(apiKey, 120000, "tools"),
        OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
    ).resolve()
}

class C04L02Tools(val aiDevsClient: AiDevsClient, val openAiClient: OpenAiClient) {



    fun resolve() {
        val question = getQuestion(aiDevsClient)
        var answer = ""
        aiDevsClient.postAnswer(answer)

    }

    private fun categorizeQuestion(question: String): String {
        val systemPrompt = """Categorize please given question to one of given categories:
            |- exchange rate
            |- country population
            |- general knowledge
            |####
            |Rules:
            |- You will solely return category name only
            |###
            |Examples:
            |Question: Jaka jest populacja Botswany?
            |Answer: country population
            |Question: Jaki był kurs PLN/EUR dnia 20.04.2010?
            |Answer: exchange rate
            |Question: Jaki jest aktualny kurs USD/GBP?
            |Answer: exchange rate
            |Question: Kto jest królem Norwegii?
            |Answer: general knowledge
            |Question: Jakie miasto jest stolicą Finlandii?
            |Answer: general knowledge
        """.trimMargin()

        return openAiClient.callCompletionApi(listOf(systemMessage(systemPrompt), userMessage(question)), 200)
            .at("/choices/0/message/content")
            .also { logger.info { "Question ($question) categorized as: $it" } }
            .asText()!!
    }

    private fun answerGeneralQuestion(question: String): String {
        val systemPrompt = """You will answer questions about general knowledge.
            |####
            |Rules:
            |- You will respond briefly as its possible
            |- You wont add any comments to answer
            |- You will answer truthfully
            |- You will answer in polish
            |###
            |Examples:
            |Question: Kto jest królem Wielkiej Brytanii?
            |Answer: Karol II
            |Question: Jakie miasto jest stolicą Finlandii?
            |Answer: Helsinki
            |Question: Kto napisał Ogniem i Mieczem?
            |Answer: Henryk Sienkiewicz""".trimMargin()

        return openAiClient.callCompletionApi(listOf(systemMessage(systemPrompt), userMessage(question)), 200)
            .at("/choices/0/message/content")
            .also { logger.info { "Answer for question ($question) is: $it" } }
            .asText()!!
    }


    private fun getQuestion(aiDevsClient: AiDevsClient): String = aiDevsClient.getTask()
        .also { logger.info { it.toString() } }
        .at("/question")
        .asText()!!
}