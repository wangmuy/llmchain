package com.wangmuy.llmchain.vectorstore

import com.wangmuy.llmchain.schema.BaseRetriever
import com.wangmuy.llmchain.schema.Document

open class VectorStoreRetriever(
    private val vectorStore: VectorStore,
    private val searchType: String = "similarity",
    private val searchArgs: Map<String, Any>? = null
): BaseRetriever {
    override fun getRelevantDocuments(query: String): List<Document> {
        return when (searchType) {
            "similarity" -> {
                vectorStore.similaritySearch(query, args = searchArgs)
            }
            "mmr" -> {
                vectorStore.maxMarginalRelevanceSearch(query, args = searchArgs)
            }
            else -> {
                throw IllegalArgumentException("searchType of $searchType not allowed.")
            }
        }
    }
}