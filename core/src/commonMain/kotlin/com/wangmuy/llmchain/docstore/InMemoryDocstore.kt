package com.wangmuy.llmchain.docstore

import com.wangmuy.llmchain.schema.Document

class InMemoryDocstore(initialDict: Map<String, Document>?): Docstore, Docstore.Addable {
    private val dict = initialDict?.toMutableMap() ?: mutableMapOf()

    override fun add(texts: Map<String, Document>) {
        val overlapping = texts.keys.intersect(dict.keys)
        if (overlapping.isNotEmpty()) {
            throw IllegalArgumentException("Tried to add ids that already exist: $overlapping")
        }
        dict.putAll(texts)
    }

    override fun search(search: String): Document? {
        return dict[search]
    }
}