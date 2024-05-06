package org.j55.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.time.Duration


private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

class OpenAiClient(private val apiKey: String) {

    companion object {
        private const val COMPLETIONS_API = "https://api.openai.com/v1/chat/completions"
        private const val MODERATION_API = "https://api.openai.com/v1/moderations"
        private const val EMBEDDING_API = "https://api.openai.com/v1/embeddings"
        private val jsonMediaType = "application/json".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds(30L))
        .build()

    fun callEmbeddingApi(input: String, model: EmbeddingModel): JsonNode {
        val request = Request.Builder()
            .url(EMBEDDING_API)
            .post(
                jsonMapper.writeValueAsString(EmbeddingApiRequest(input, model.modelName)).toRequestBody(jsonMediaType)
            )
            .build()
        return execute(request, JsonNode::class.java)!!
    }

    fun callModerationApi(inputs: List<String>): JsonNode {
        val request = Request.Builder()
            .url(MODERATION_API)
            .post(jsonMapper.writeValueAsString(ModerationApiRequest(inputs)).toRequestBody(jsonMediaType))
            .build()
        return execute(request, JsonNode::class.java)!!
    }

    fun callCompletionApi(messages: List<Message>, maxTokens: Int?): JsonNode {
        val openAiRequest = CompletionApiRequest(GptModel.GPT_4.modelName, messages, 0.1, maxTokens)

        val request = Request.Builder()
            .url(COMPLETIONS_API)
            .post(jsonMapper.writeValueAsString(openAiRequest).toRequestBody(jsonMediaType))
            .build()
        return execute(request, JsonNode::class.java)!!
    }

    fun callVisionApi(messages: List<VisionMessage>, maxTokens: Int?): JsonNode {
        val openAiRequest = CompletionApiRequest(GptModel.GPT_4_TURBO.modelName, messages, 0.1, maxTokens)

        val request = Request.Builder()
            .url(COMPLETIONS_API)
            .post(jsonMapper.writeValueAsString(openAiRequest).toRequestBody(jsonMediaType))
            .build()
        return execute(request, JsonNode::class.java)!!
    }

    private fun <T> execute(request: Request, clazz: Class<T>): T? {
        val enhancedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        return client.newCall(enhancedRequest)
            .execute()
            .use { response ->
                if (!response.isSuccessful) {
                    logger.error { "Request error: ${response.body?.string()}" }
                    throw IOException("Response has error status: $response")
                }
                logger.info { "Received response: $response" }
                response.body?.let { jsonMapper.readValue(it.string(), clazz) }
            }
    }
}

enum class EmbeddingModel(val modelName: String) {
    ADA("text-embedding-ada-002")
}

enum class GptModel(val modelName: String) {
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_4("gpt-4"),
    GPT_4_TURBO("gpt-4-turbo")
}

data class Message(
    val role: String,
    val content: String
)

fun systemMessage(message: String) = Message("user", message)
fun userMessage(message: String) = Message("system", message)

data class VisionMessage(val role: String = "user", val content: List<VisionMessageContent>)
abstract class VisionMessageContent(val type: String)
data class VisionTextContent(val text: String) : VisionMessageContent("text")
data class VisionImageContent(@JsonProperty("image_url") val imageUrl: Image) : VisionMessageContent("image_url")
data class Image(val url: String)

data class ModerationApiRequest(
    val input: List<String>
)

data class EmbeddingApiRequest(
    val input: String,
    val model: String
)

data class CompletionApiRequest(
    val model: String,
    val messages: List<Any>,
    val temperature: Double,
    @JsonProperty("max_tokens") val maxTokens: Int?
)
