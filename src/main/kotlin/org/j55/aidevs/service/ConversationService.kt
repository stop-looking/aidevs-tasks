package org.j55.aidevs.service

import org.j55.org.j55.aidevs.api.model.Answer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.Generation
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatClient
import org.springframework.stereotype.Service

@Service
class ConversationService(
    private val openAiApi: OpenAiChatClient
) {
    val logger: Logger = LoggerFactory.getLogger(ConversationService::class.java)
    val memory = mutableListOf<String>()

    fun callConversation(question: String): String {
        return openAiApi.call(question)
            .also { logger.info(it) }
            .toString()
    }

    fun answerQuestion(question: String): Answer = when (recognizeIntention(question)) {
        "QUESTION" -> callConversationWithContext(question)
        "COMMAND" -> callConversationWithContext(question)
        "INFORMATION" -> rememberInformation(question)
        else -> throw IllegalArgumentException("Unknown intention: $question")
    }

    private fun rememberInformation(information: String): Answer {
        memory.add(information)
        return Answer("OK, remembered!")
    }

    private fun callConversationWithContext(question: String): Answer {
        val prompt = Prompt(
            listOf(
                SystemMessage("Additional context to answer a question: $memory"),
                UserMessage(question)
            )
        )
        return Answer(openAiApi.call(prompt).result.toString())
    }

    private fun recognizeIntention(question: String): String? {
        val prompt = Prompt(
            listOf(
                SystemMessage("Recognize if user input is a command, a question or an information. Do not answer, if user prompt contains a question. Respond only with QUESTION|INFORMATION|COMMAND"),
                UserMessage(question)
            )
        )
        return openAiApi.call(prompt).result!!
            .output
            .content!!
            .also { logger.info("Recognized intention: $it") }
    }

}