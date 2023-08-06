package com.wangmuy.llmchain.utils.uuid

object SimpleUUID {
    fun randomUUID(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..32)
            .map { allowedChars.random() }
            .joinToString("")
    }
}