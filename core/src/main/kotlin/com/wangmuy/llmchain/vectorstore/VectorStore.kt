package com.wangmuy.llmchain.vectorstore

import com.wangmuy.llmchain.embedding.Embeddings
import com.wangmuy.llmchain.schema.BaseRetriever
import com.wangmuy.llmchain.schema.Document

interface VectorStore {
    fun addTexts(
        texts: Iterable<String>,
        metaDatas: List<Map<String, Any>?>? = null,
        args: Map<String, Any>? = null
    ): List<String>

    fun addDocuments(documents: List<Document>, args: Map<String, Any>? = null): List<String> {
        val texts = documents.map { it.pageContent }
        val metaDatas = documents.map { it.metadata }
        return addTexts(texts, metaDatas, args)
    }

    fun similaritySearch(
        query: String,
        k: Int = 4,
        args: Map<String, Any>? = null
    ): List<Document>

    fun similaritySearchByVector(
        embedding: Array<Float>,
        k: Int = 4,
        args: Map<String, Any>? = null
    ): List<Document>

    fun maxMarginalRelevanceSearch(
        query: String,
        k: Int = 4,
        fetchK: Int = 20,
        args: Map<String, Any>? = null
    ): List<Document>

    fun maxMarginalRelevanceSearchByVector(
        embedding: Array<Float>,
        k: Int = 4,
        fetchK: Int = 20,
        args: Map<String, Any>? = null
    ): List<Document>

    fun asRetriever(args: Map<String, Any>? = null): BaseRetriever {
        return VectorStoreRetriever(vectorStore = this, searchArgs = args)
    }

    interface Builder<T: VectorStore> {
        fun fromTexts(
            texts: List<String>,
            embedding: Embeddings,
            metaDatas: List<Map<String, String>?>? = null,
            args: Map<String, Any>? = null
        ): T
    }
}