package com.wangmuy.llmchain.embedding

interface Embeddings {
    fun embedDocuments(texts: List<String>): List<Array<Float>>
    fun embedQuery(text: String): Array<Float>
}

typealias EmbeddingFunc = (List<String>) -> List<Array<Float>>