package com.wangmuy.llmchain.serviceprovider.openai

import com.theokanning.openai.embedding.EmbeddingRequest
import com.wangmuy.llmchain.embedding.Embeddings
import java.net.Proxy

class OpenAIEmbedding(
    apiKey: String,
    proxy: Proxy? = null
): Embeddings {
    init {
        ServiceHolder.apiKey = apiKey
        ServiceHolder.proxy = proxy
    }

    override fun embedDocuments(texts: List<String>): List<Array<Float>> {
        val openAIService = ServiceHolder.openAiService
        val request = EmbeddingRequest.builder()
            .model("text-similarity-babbage-001")
            .input(texts)
            .build()
        val embeddings = openAIService.createEmbeddings(request).data
        return embeddings.map {emb-> emb.embedding.map { it.toFloat() }.toTypedArray() }
    }

    override fun embedQuery(text: String): Array<Float> {
        return embedDocuments(listOf(text))[0]
    }
}