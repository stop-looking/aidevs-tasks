package org.j55.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

private const val TASKS_AIDEVS_URL = "https://tasks.aidevs.pl"

class AiDevsClient(private val apiKey: String, tokenTtlMillis: Long, private val taskName: String) {
    private val tokenProvider = TokenProvider(tokenTtlMillis, this::getAuthToken)
    private val jsonMediaType = "application/json".toMediaType()
    private val client = OkHttpClient().newBuilder()
        .readTimeout(45L, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun getAuthToken(): String {
        val request = Request.Builder()
            .url("$TASKS_AIDEVS_URL/token/$taskName")
            .post(jsonMapper.writeValueAsString(AiDevsAuth(apiKey)).toRequestBody(jsonMediaType))
            .build()

        logger.info { "Sending request: $request" }
        return execute(request, AiDevsAuthResponse::class.java).token
    }

    fun getTask(): JsonNode {
        val request = Request.Builder()
            .url("$TASKS_AIDEVS_URL/task/${tokenProvider.get()}")
            .build()

        return execute(request, JsonNode::class.java)
    }

    fun getTask(formParams: Map<String, String>): JsonNode {
        val request = Request.Builder()
            .post(FormBody.Builder()
                .also { formParams.forEach { entry -> it.add(entry.key, entry.value) } }
                .build())
            .url("$TASKS_AIDEVS_URL/task/${tokenProvider.get()}")
            .build()

        return execute(request, JsonNode::class.java)
    }

    suspend fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            .get()
            .build()
        return flow { emit(execute(request, String::class.java)) }
            .retry(10)
            .firstOrNull()!!
    }

    fun postAnswer(answer: Any): JsonNode {
        val request = Request.Builder()
            .post(
                when (answer) {
                    is String -> answer
                    else -> jsonMapper.writeValueAsString(answer).also { logger.info { "Answer: $it" } }
                }.toRequestBody(jsonMediaType)
            )
            .url("$TASKS_AIDEVS_URL/answer/${tokenProvider.get()}")
            .build()

        return execute(request, JsonNode::class.java).also { logger.info { "Response from answer api: $it" } }
    }

    private fun <T> execute(request: Request, clazz: Class<T>): T {
        logger.info { "Executing request: $request" }
        client.newCall(request)
            .execute()
            .use { response ->
                if (!response.isSuccessful) {
                    logger.error { "Http error ${response.code}: ${response.body?.string()}" }
                    throw IOException("Response has error status: $response")
                }
                logger.info { "Received response: $response" }
                return when {
                    clazz.isAssignableFrom(String::class.java) -> response.body!!.string() as T
                    else -> response.body?.let { jsonMapper.readValue(it.string(), clazz) }!!
                }
            }
    }

    class TokenProvider(private val ttlMillis: Long, private val provider: () -> String) {
        private var token: String = ""
        private var lastRetrieved: Long = 0

        fun get(): String {
            if (System.currentTimeMillis() - lastRetrieved > ttlMillis) {
                refreshToken()
            }
            return token
        }

        private fun refreshToken() {
            logger.info { "Refreshing token" }
            token = provider.invoke()
            this.lastRetrieved = System.currentTimeMillis()
        }

    }
}

data class AiDevsAuth(val apikey: String)
data class AiDevsAuthResponse(val code: Int, val msg: String, val token: String)
data class AiDevsAnswer(val answer: Any)