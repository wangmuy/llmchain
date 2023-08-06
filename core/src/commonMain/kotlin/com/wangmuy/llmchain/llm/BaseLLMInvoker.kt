package com.wangmuy.llmchain.llm

interface BaseLLMInvoker {
    fun invoke(prompt: String, stop: List<String>?): String
}