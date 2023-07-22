package com.wangmuy.llmchain.serviceprovider.openai

import com.theokanning.openai.embedding.EmbeddingRequest
import com.theokanning.openai.service.OpenAiService
import com.wangmuy.llmchain.embedding.Embeddings
import java.net.Proxy

class OpenAIEmbedding(
    apiKey: String,
    baseUrl: String = OpenAIChat.OPENAI_BASE_URL,
    timeoutMillis: Long = ServiceInfo.TIMEOUT_MILLIS,
    proxy: Proxy? = null
): Embeddings {
    private val openAiService: OpenAiService
    init {
        ServiceHolder.serviceInfo.baseUrl = baseUrl
        ServiceHolder.serviceInfo.apiKey = apiKey
        ServiceHolder.serviceInfo.timeoutMillis = timeoutMillis
        ServiceHolder.serviceInfo.proxy = proxy
        openAiService = ServiceHolder.serviceInfo.service
    }

    override fun embedDocuments(texts: List<String>): List<Array<Float>> {
        val request = EmbeddingRequest.builder()
            .model("text-similarity-babbage-001")
            .input(texts)
            .build()
        val embeddings = openAiService.createEmbeddings(request).data
        return embeddings.map {emb-> emb.embedding.map { it.toFloat() }.toTypedArray() }
    }

    override fun embedQuery(text: String): Array<Float> {
        return embedDocuments(listOf(text))[0]
    }
}