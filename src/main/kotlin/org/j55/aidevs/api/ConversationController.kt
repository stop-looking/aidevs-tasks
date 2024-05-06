package org.j55.aidevs.api

import org.j55.aidevs.api.model.Question
import org.j55.aidevs.service.ConversationService
import org.j55.org.j55.aidevs.api.model.Answer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/conversation")
class ConversationController(
    private val conversationService: ConversationService
) {

    val logger: Logger = LoggerFactory.getLogger(ConversationController::class.java)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getConversation(@RequestBody question: Question?): Answer {
        logger.info("Received question: $question")
        return when (question) {
            null -> Answer("question is null")
            else -> handleQuestion(question)
        }.also { logger.info("Conversation answer: ($it)") }
    }

    @PostMapping("/2")
    fun getConversationPro(@RequestBody question: Question): Answer {
        logger.info("Received question to pro: $question")
        return conversationService.answerQuestion(question.question!!)
            .also { logger.info("ProConversation answer: ($it)") }
    }


    private fun handleQuestion(question: Question): Answer = when (question.question) {
        null -> Answer("question body is null")
        else -> Answer(conversationService.callConversation(question.question))
    }


}