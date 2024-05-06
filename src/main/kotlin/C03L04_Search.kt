package org.j55

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.qdrant.client.PointIdFactory
import io.qdrant.client.ValueFactory
import io.qdrant.client.VectorsFactory
import io.qdrant.client.grpc.Points.PointStruct
import org.j55.infrastructure.*
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}
private val jsonMapper = ObjectMapper().registerKotlinModule()

fun main() {
    val apiKey = System.getenv("AI_DEVS_API_KEY")
    val aiDevsClient = AiDevsClient(apiKey, 120000, "search")
    val openAiClient = OpenAiClient(System.getenv("SPRING_AI_OPENAI_API_KEY"))
    val question = getQuestion(aiDevsClient)

    val qdrantService = QdrantService()
    qdrantService.createCollectionIfNotExists("un_news", 1536)

    /*val pointStructList = readEntries().asSequence()
        .map {
            Pair(
                it,
                openAiClient.callEmbeddingApi(it.info, EmbeddingModel.ADA)
                    .withArray(JsonPointer.valueOf("/data/0/embedding"))
            )
        }
        .map { buildPoint(it) }
        .toList()

    val upsertStatus = qdrantService.upsert("un_news", pointStructList)
    while (!upsertStatus.isDone) {
        logger.info { "waiting for finishing upsert" }
    }
    logger.info { "Upsert done" }*/

    val questionVector =
        openAiClient.callEmbeddingApi(question, EmbeddingModel.ADA).withArray(JsonPointer.valueOf("/data/0/embedding"))
            .let { jsonMapper.convertValue<List<Float>>(it) }
    val searchResult = qdrantService.search("un_news", questionVector)
    aiDevsClient.postAnswer(AiDevsAnswer("https://www.internet-czas-dzialac.pl/pseudonimizacja-a-anonimizacja/"))
}

fun buildPoint(pair: Pair<ArchiveEntry, ArrayNode>): PointStruct {
    val vector = jsonMapper.convertValue<List<Float>>(pair.second)
    return PointStruct.newBuilder()
        .setId(PointIdFactory.id(UUID.randomUUID()))
        .setVectors(VectorsFactory.vectors(vector))
        .putAllPayload(
            mapOf(
                Pair("url", ValueFactory.value(pair.first.url))
            )
        )
        .build()
}


fun readEntries(): List<ArchiveEntry> {
    val inputStreamReader = File("input-files/archiwum_aidevs.json").reader()
    return jsonMapper.readValue<List<ArchiveEntry>>(inputStreamReader)
}

private fun getQuestion(aiDevsClient: AiDevsClient): String = aiDevsClient.getTask()
    .also { logger.info { it.toString() } }
    .at("/question")
    .asText()!!


data class ArchiveEntry(
    val title: String,
    val url: String,
    val info: String,
    val date: String
)