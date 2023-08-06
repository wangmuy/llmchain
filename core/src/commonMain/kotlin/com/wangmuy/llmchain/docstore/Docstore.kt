package com.wangmuy.llmchain.docstore

import com.wangmuy.llmchain.schema.Document

interface Docstore {
    fun search(search: String): Document?

    interface Addable {
        fun add(texts: Map<String, Document>)
    }
}