package com.wangmuy.llmchain.serviceprovider.openai

import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.wangmuy.llmchain.embedding.Embeddings
import kotlinx.coroutines.runBlocking

class OpenAIEmbedding(
    apiKey: String,
    baseUrl: String = OpenAIChat.OPENAI_BASE_URL,
    timeoutMillis: Long = ServiceInfo.TIMEOUT_MILLIS,
    proxy: String? = null
): Embeddings {
    private val openAiService: OpenAI
    init {
        ServiceHolder.serviceInfo.baseUrl = baseUrl
        ServiceHolder.serviceInfo.apiKey = apiKey
        ServiceHolder.serviceInfo.timeoutMillis = timeoutMillis
        ServiceHolder.serviceInfo.proxy = proxy
        openAiService = ServiceHolder.serviceInfo.service
    }

    override fun embedDocuments(texts: List<String>): List<Array<Float>> {
        val request = EmbeddingRequest(
            model = ModelId("text-similarity-babbage-001"),
            input = texts
        )
        val embeddings = runBlocking { openAiService.embeddings(request).embeddings }
        return embeddings.map {emb-> emb.embedding.map { it.toFloat() }.toTypedArray() }
    }

    override fun embedQuery(text: String): Array<Float> {
        return embedDocuments(listOf(text))[0]
    }
}