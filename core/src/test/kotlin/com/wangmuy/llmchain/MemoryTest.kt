package com.wangmuy.llmchain

import com.wangmuy.llmchain.memory.SimpleMemory
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryTest {
    @Test fun testSimpleMemory() {
        val memory = SimpleMemory(mutableMapOf("baz" to "foo"))
        val output = memory.loadMemoryVariables(emptyMap())
        assertEquals(mapOf("baz" to "foo"), output)
        assertEquals(listOf("baz"), memory.memoryVariables())
    }
}