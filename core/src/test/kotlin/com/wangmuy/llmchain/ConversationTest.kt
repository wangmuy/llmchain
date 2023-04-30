package com.wangmuy.llmchain

import com.wangmuy.llmchain.chain.ConversationChain
import com.wangmuy.llmchain.memory.ConversationBufferMemory
import com.wangmuy.llmchain.prompt.PromptTemplate
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTest {
    @Test fun testMemoryAIPrefix() {
        val memory = ConversationBufferMemory(memoryKey = "foo", aiPrefix = "Assistant")
        memory.saveContext(mapOf("input" to "bar"), mapOf("output" to "foo"))
        assertEquals("Human: bar\nAssistant: foo", memory.getBuffer()[0].content)
    }

    @Test fun testMemoryHumanPrefix() {
        val memory = ConversationBufferMemory(memoryKey = "foo", humanPrefix = "Friend")
        memory.saveContext(mapOf("input" to "bar"), mapOf("output" to "foo"))
        assertEquals("Friend: bar\nAI: foo", memory.getBuffer()[0].content)
    }

    @Test fun testConversationChainWorks() {
        val llm = FakeLLM()
        val prompt = PromptTemplate(listOf("foo", "bar"), "{foo} {bar}")
        val memory = ConversationBufferMemory(memoryKey = "foo")
        val chain = ConversationChain(llm, prompt, memory, "bar")
        val result = chain.run(mapOf("input" to "foo"))
        assertEquals("foo", result)
    }
}