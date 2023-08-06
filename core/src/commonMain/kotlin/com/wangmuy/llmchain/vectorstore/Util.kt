package com.wangmuy.llmchain.vectorstore

import kotlin.jvm.JvmStatic
import kotlin.math.sqrt

class Util {
    companion object {
        @JvmStatic
        fun cosineSimilarity(a: Array<Float>, b: Array<Float>): Float {
            var dotProduct = 0.0f
            var normA = 0.0f
            var normB = 0.0f
            a.forEachIndexed { i, ea ->
                val eb = b[i]
                dotProduct += ea * eb
                normA += ea * ea
                normB += eb * eb
            }
            return dotProduct / (sqrt(normA) * sqrt(normB))
        }

        @JvmStatic
        fun maximalMarginalRelevance(
            queryEmbedding: Array<Float>,
            embeddingList: List<Array<Float>>,
            lambdaMult: Float = 0.5f,
            k: Int = 4
        ): List<Int> {
            val idxList = mutableListOf<Int>()
            while (idxList.size < k) {
                var bestScore = -Float.MAX_VALUE
                var idxToAdd = -1
                for((i, emb) in embeddingList.withIndex()) {
                    if (i in idxList) {
                        continue
                    }
                    val firstPart = cosineSimilarity(queryEmbedding, emb)
                    var secondPart = 0f
                    for (j in idxList) {
                        val cosSim = cosineSimilarity(emb, embeddingList[j])
                        if (cosSim > secondPart) {
                            secondPart = cosSim
                        }
                    }
                    val equationScore = lambdaMult * firstPart - (1 - lambdaMult) * secondPart
                    if (equationScore > bestScore) {
                        bestScore = equationScore
                        idxToAdd = i
                    }
                }
                idxList.add(idxToAdd)
            }
            return idxList
        }
    }
}