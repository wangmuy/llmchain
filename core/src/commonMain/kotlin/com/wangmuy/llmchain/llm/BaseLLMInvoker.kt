package com.wangmuy.llmchain.llm

interface BaseLLMInvoker {
    fun invoke(prompt: String, stop: List<String>?, inputList: List<Map<String, Any>>): String
}