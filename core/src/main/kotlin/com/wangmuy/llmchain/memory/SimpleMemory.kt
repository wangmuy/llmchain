package com.wangmuy.llmchain.memory

import com.wangmuy.llmchain.schema.BaseMemory

class SimpleMemory @JvmOverloads constructor(
    val memories: MutableMap<String, Any> = HashMap()): BaseMemory() {
    override fun memoryVariables(): List<String> {
        return memories.keys.toList()
    }

    override fun loadMemoryVariables(inputs: Map<String, Any>): Map<String, Any> {
        return memories
    }

    override fun saveContext(inputs: Map<String, Any>, outputs: Map<String, String>) {
    }

    override fun clear() {
    }
}