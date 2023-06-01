package com.wangmuy.llmchain.vectorstore

import com.wangmuy.llmchain.embedding.EmbeddingFunc
import com.wangmuy.llmchain.embedding.Embeddings
import com.wangmuy.llmchain.schema.Document
import java.util.*
import kotlin.math.sqrt

typealias DistanceFunc = (Array<Float>, Array<Float>) -> Float

class SimpleVectorStore(
    collectionName: String,
    val embeddingFunc: Embeddings,
    collectionMetaData: Map<String, Any>? = null,
    val index: Index = SimpleIndex(collectionName)
): VectorStore {
    interface Index {
        fun add(uuids: List<String>, embeddings: List<Array<Float>>)

        fun getNearestNeighbors(
            embedding: Array<Float>, nResults: Int,
            ids: List<String>, args: Map<String, Any>? = null
        ): Pair<List<String>, List<Float>>
    }

    class VSCollection(
        val uuid: String,
        val name: String,
        val metaData: Map<String, Any>? = null,
        val embeddingFunc: EmbeddingFunc? = null)

    class VSEmbedding(
        val collectionUUID: String,
        val uuid: String, // python uuid4
        val embedding: Array<Float>? = null,
        val document: String = "",
        val id: String? = null, // python uuid1
        val metaData: Map<String, Any>? = null)

    class SimpleIndex(private val collectionName: String): Index {
        companion object {
            const val KEY_DISTANCE_FUNC = "distanceFunc"
            const val KEY_DISTANCE_COMPARATOR = "distanceComparator"

            /** collectionName to Map<uuid to embeddingArrayFloat> */
            private val EMBEDDINGS: MutableMap<String, MutableMap<String, Array<Float>>> = mutableMapOf()

            val DEFAULT_DISTANCE_FUNC = object: DistanceFunc {
                override fun invoke(emb1: Array<Float>, emb2: Array<Float>): Float {
                    return sqrt(emb1.zip(emb2).map {
                        val x = it.first - it.second
                        x * x
                    }.reduce { x1, x2 -> x1 + x2 })
                }
            }

            val LESSER_COMPARATOR = Comparator<Float> { a, b -> a.compareTo(b) }

            private class QueueNode(val uuid: String, val embedding: Array<Float>)

            @JvmStatic
            fun findNearestNeighbors(
                embedding: Array<Float>, collection: Map<String, Array<Float>>, nFirst: Int,
                distanceFunc: (Array<Float>, Array<Float>) -> Float,
                distanceComparator: Comparator<Float>
            ): Pair<List<String>, List<Float>> {
                val nodes = collection.map { QueueNode(it.key, it.value) }
                val distanceMap = mutableMapOf<QueueNode, Float>()
                val queue = PriorityQueue<QueueNode> { node1, node2 ->
                    val d1 = distanceMap.getOrPut(node1) {
                        distanceFunc(embedding, node1.embedding)
                    }
                    val d2 = distanceMap.getOrPut(node2) {
                        distanceFunc(embedding, node2.embedding)
                    }
                    distanceComparator.compare(d1, d2)
                }
                nodes.forEach {
                    queue.add(it)
                    if (queue.size > nFirst) {
                        // pop head
                        queue.poll()
                    }
                }
                val embList = mutableListOf<QueueNode>()
                val distList = mutableListOf<Float>()
                while(queue.size > 0) {
                    val emb = queue.poll()!!
                    embList.add(emb)
                    distList.add(distanceMap.getOrPut(emb) {
                        distanceFunc(embedding, emb.embedding)
                    })
                }
                return Pair(embList.map { it.uuid }, distList)
            }
        }

        private val collection: MutableMap<String, Array<Float>>

        init {
            synchronized(EMBEDDINGS) {
                collection = EMBEDDINGS.getOrPut(collectionName) { mutableMapOf() }
            }
        }

        override fun add(uuids: List<String>, embeddings: List<Array<Float>>) {
            synchronized(EMBEDDINGS) {
                collection.putAll(uuids.zip(embeddings))
            }
        }

        override fun getNearestNeighbors(
            embedding: Array<Float>, nResults: Int,
            ids: List<String>, args: Map<String, Any>?
        ): Pair<List<String>, List<Float>> {
            val distanceFunc = args?.get(KEY_DISTANCE_FUNC) as DistanceFunc? ?: DEFAULT_DISTANCE_FUNC
            val distanceComparator = args?.get(KEY_DISTANCE_COMPARATOR) as Comparator<Float>? ?: LESSER_COMPARATOR.reversed()
            val collection = synchronized(EMBEDDINGS) {
                collection.filterKeys { it in ids }
            }
            val (embList, distances) = findNearestNeighbors(
                embedding, collection, nResults, distanceFunc, distanceComparator)
            return Pair(embList, distances)
        }
    }

    companion object {
        const val KEY_IDS = "ids"
        const val KEY_FILTER = "filter"

        /** collectionName to VSCollection */
        private val COLLECTIONS: MutableMap<String, VSCollection> = mutableMapOf()
        /** collectionName to VSEmbedding */
        private val EMBEDDINGS: MutableMap<String, MutableList<VSEmbedding>> = mutableMapOf()
        private val LOCK = Object()

        private fun getOrCreateCollection(
            collectionName: String, embeddingFunc: EmbeddingFunc?, metaData: Map<String, Any>?): VSCollection {
            synchronized(LOCK) {
                return COLLECTIONS.getOrPut(collectionName) {
                    val uuid = UUID.randomUUID().toString() // uuid4
                    VSCollection(uuid, collectionName, metaData, embeddingFunc)
                }
            }
        }
    }

    private val collection: VSCollection

    init {
        val embedDocumentsFunc = embeddingFunc::embedDocuments
        collection = getOrCreateCollection(collectionName, embedDocumentsFunc, collectionMetaData)
    }

    override fun addTexts(
        texts: Iterable<String>,
        metaDatas: List<Map<String, Any>?>?,
        args: Map<String, Any>?
    ): List<String> {
        // uuid1
        val ids = (args?.get(KEY_IDS) ?: texts.map { UUID.randomUUID().toString() }.toList()) as List<String>
        val embeddings = embeddingFunc.embedDocuments(texts.toList())
        val addedUUIDs = mutableListOf<String>()
        // _collection.add() -> LocalAPI._db.add()
        synchronized(LOCK) {
            val collectionUUID = collection.uuid
            // INSERT INTO embeddings
            texts.forEachIndexed { i, document ->
                val uuid = UUID.randomUUID().toString() // uuid4
                addedUUIDs.add(uuid)
                val embedding = embeddings[i]
                val metaData = metaDatas?.get(i)
                val id = ids[i]
                EMBEDDINGS.getOrPut(collection.name){ mutableListOf() }
                    .add(VSEmbedding(collectionUUID, uuid, embedding, document, id, metaData))
            }
        }
        // LocalAPI._db.add_incremental()
        index.add(addedUUIDs, embeddings)
        return ids
    }

    override fun similaritySearch(query: String, k: Int, args: Map<String, Any>?): List<Document> {
        val queryEmbedding = embeddingFunc.embedQuery(query)
        return similaritySearchWithScore(queryEmbedding, k, args).map { it.first }
    }

    /**
     * filter: Filter by metadata
     */
    fun similaritySearchWithScore(
        queryEmbedding: Array<Float>, k: Int, args: Map<String, Any>? = null
    ): List<Pair<Document, Float>> {
        // _collection.query() -> _client.query() -> uuids, distances = _db.get_nearest_neighbors()
        // db_results = _db.get_by_ids(uuids): SELECT FROM embeddings where uuid IN (ids...)
        // assemble QueryResult(db_results{["id"], ["embedding"], ["document"], ["metadata"]}, distance)
        val filter = args?.get(KEY_FILTER) as Map<String, String>?
        val (embList, distanceList) = getNearestNeighbors(filter, queryEmbedding, k, args)
        return embList.map { Document(it.document, it.metaData) }.zip(distanceList)
    }

    private fun getNearestNeighbors(
        whereFilter: Map<String, String>?, embeddings: Array<Float>,
        nResults: Int, args: Map<String, Any>? = null
    ): Pair<List<VSEmbedding>, List<Float>> { // uuids, distances
        // results = SQLite.get(collection_uuid, where, where_document): SELECT FROM embeddings
        val embList = synchronized(LOCK) {
            EMBEDDINGS.getOrPut(collection.name) { mutableListOf() }.filter {emb->
                if (whereFilter != null && whereFilter.isNotEmpty()) {
                    emb.metaData != null && emb.metaData.isNotEmpty()
                            && emb.metaData.entries.containsAll(whereFilter.entries)
                } else {
                    true
                }
            }
        }
        // uuids, distances = _index(collection_uuid).get_nearest_neighbors(embeddings, n_results, ids=[x[1] for x in results])
        val (uuids, distances) = index.getNearestNeighbors(embeddings, nResults, embList.map { it.uuid }, args)
        return Pair(embList.filter { it.uuid in uuids }, distances)
    }

    override fun similaritySearchByVector(
        embedding: Array<Float>,
        k: Int,
        args: Map<String, Any>?
    ): List<Document> {
        return similaritySearchWithScore(embedding, k, args).map { it.first }
    }

    override fun maxMarginalRelevanceSearch(
        query: String,
        k: Int,
        fetchK: Int,
        args: Map<String, Any>?
    ): List<Document> {
        val embedding = embeddingFunc.embedQuery(query)
        return maxMarginalRelevanceSearchByVector(embedding, k, fetchK, args)
    }

    override fun maxMarginalRelevanceSearchByVector(
        embedding: Array<Float>,
        k: Int,
        fetchK: Int,
        args: Map<String, Any>?
    ): List<Document> {
        val (results, _) = getNearestNeighbors(
            args?.get(KEY_FILTER) as Map<String, String>?, embedding, fetchK, args)
        val mmrSelected = Util.maximalMarginalRelevance(
            embedding, results.map { it.embedding!! }, k = k)
        return results.filterIndexed { i, _ -> i in mmrSelected }
            .map { Document(it.document, it.metaData) }
    }

    class Builder: VectorStore.Builder<SimpleVectorStore> {
        companion object {
            const val KEY_COLLECTION_NAME = "collectionName"
        }

        override fun fromTexts(
            texts: List<String>,
            embedding: Embeddings,
            metaDatas: List<Map<String, String>?>?,
            args: Map<String, Any>?
        ): SimpleVectorStore {
            val collectionName = args?.get(KEY_COLLECTION_NAME) as String? ?: "langchain"
            val store = SimpleVectorStore(collectionName, embedding, null)
            store.addTexts(texts, metaDatas, args)
            return store
        }
    }
}