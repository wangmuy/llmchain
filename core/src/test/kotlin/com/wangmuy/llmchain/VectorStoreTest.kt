package com.wangmuy.llmchain

import com.wangmuy.llmchain.embedding.Embeddings
import com.wangmuy.llmchain.schema.Document
import com.wangmuy.llmchain.vectorstore.SimpleVectorStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeEmbeddings: Embeddings {
    override fun embedDocuments(texts: List<String>): List<Array<Float>> {
        return List(texts.size) { i-> arrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, i.toFloat())  }
    }

    override fun embedQuery(text: String): Array<Float> {
        return arrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 0f)
    }
}

class VectorStoreTest {
    @Test fun nearestNeighborL2Test() {
        val embdding = arrayOf(0.03f, 0.025f, 0.01f)
        val collection = mapOf(
            "uuid1" to arrayOf(0.01f, 0.02f, 0.03f),
            "uuid2" to arrayOf(0.03f, 0.02f, 0.01f)
        )
        val results = SimpleVectorStore.SimpleIndex.findNearestNeighbors(embdding, collection, 1,
            SimpleVectorStore.SimpleIndex.DEFAULT_DISTANCE_FUNC, SimpleVectorStore.SimpleIndex.LESSER_COMPARATOR.reversed())
        println("distances=${results.second}")
        assertTrue(results.first.size == 1)
        assertTrue(results.first[0] == "uuid2")
    }

    @Test fun testSimpleVectorStore() {
        val texts = listOf("foo", "bar", "baz")
        val docSearch = SimpleVectorStore.Builder().fromTexts(
            texts, FakeEmbeddings(),
            args = mapOf(SimpleVectorStore.Builder.KEY_COLLECTION_NAME to "test_collection")
        )
        val output = docSearch.similaritySearch("foo", k=1)
        assertTrue(output.size == 1)
        assertEquals(listOf(Document("foo")), output)
    }

    @Test fun testSimpleVectorStoreWithMetaDatas() {
        val texts = listOf("foo", "bar", "baz")
        val metaDatas = List(texts.size) { i -> mapOf("page" to i.toString()) }
        val docSearch = SimpleVectorStore.Builder().fromTexts(
            texts, FakeEmbeddings(), metaDatas,
            args = mapOf(SimpleVectorStore.Builder.KEY_COLLECTION_NAME to "test_collection")
        )
        val output = docSearch.similaritySearch("foo", k=1)
        assertTrue(output.size == 1)
        assertEquals(listOf(Document("foo", mapOf("page" to "0"))), output)
    }

    @Test fun testSimpleVectorStoreWithMetaDatasWithScores() {
        val texts = listOf("foo", "bar", "baz")
        val metaDatas = List(texts.size) { i -> mapOf("page" to i.toString()) }
        val docSearch = SimpleVectorStore.Builder().fromTexts(
            texts, FakeEmbeddings(), metaDatas,
            args = mapOf(SimpleVectorStore.Builder.KEY_COLLECTION_NAME to "test_collection")
        )
        val queryEmbedding = docSearch.embeddingFunc.embedQuery("foo")
        val output = docSearch.similaritySearchWithScore(queryEmbedding, k=1)
        assertTrue(output.size == 1)
        assertEquals(listOf(Pair(Document("foo", mapOf("page" to "0")), 0f)), output)
    }

    @Test fun testSimpleVectorStoreSearchFilter() {
        val texts = listOf("far", "bar", "baz")
        val metaDatas = texts.map { mapOf("first_letter" to it[0].toString()) }
        val docSearch = SimpleVectorStore.Builder().fromTexts(
            texts, FakeEmbeddings(), metaDatas,
            args = mapOf(SimpleVectorStore.Builder.KEY_COLLECTION_NAME to "test_collection")
        )
        var output = docSearch.similaritySearch("far", k=1,
            mapOf(SimpleVectorStore.KEY_FILTER to mapOf("first_letter" to "f")))
        assertTrue(output.size == 1)
        assertEquals(listOf(Document("far", mapOf("first_letter" to "f"))), output)

        output = docSearch.similaritySearch("far", k=1,
            mapOf(SimpleVectorStore.KEY_FILTER to mapOf("first_letter" to "b")))
        assertTrue(output.size == 1)
        assertEquals(listOf(Document("bar", mapOf("first_letter" to "b"))), output)
    }

    @Test fun testSimpleVectorStoreFilterWithScores() {
        val texts = listOf("far", "bar", "baz")
        val metaDatas = texts.map { mapOf("first_letter" to it[0].toString()) }
        val docSearch = SimpleVectorStore.Builder().fromTexts(
            texts, FakeEmbeddings(), metaDatas,
            args = mapOf(SimpleVectorStore.Builder.KEY_COLLECTION_NAME to "test_collection")
        )
        var queryEmbedding = docSearch.embeddingFunc.embedQuery("far")
        var output = docSearch.similaritySearchWithScore(queryEmbedding, k=1,
            mapOf(SimpleVectorStore.KEY_FILTER to mapOf("first_letter" to "f")))
        assertTrue(output.size == 1)
        assertEquals(listOf(Pair(Document("far", mapOf("first_letter" to "f")), 0f)), output)

        queryEmbedding = docSearch.embeddingFunc.embedQuery("far")
        output = docSearch.similaritySearchWithScore(queryEmbedding, k=1,
            mapOf(SimpleVectorStore.KEY_FILTER to mapOf("first_letter" to "b")))
        assertTrue(output.size == 1)
        assertEquals(listOf(Pair(Document("bar", mapOf("first_letter" to "b")), 1f)), output)
    }
}