package com.wangmuy.llmchain

import com.wangmuy.llmchain.chain.Chain
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeChain(
    private val beCorrect: Boolean = true,
    private val theInputKeys: List<String> = listOf("foo"),
    private val theOutputKeys: List<String> = listOf("bar")
): Chain() {
    override fun inputKeys(): List<String>? {
        return theInputKeys
    }

    override fun outputKeys(): List<String> {
        return theOutputKeys
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        return if (beCorrect)
            mapOf("bar" to "baz")
        else
            mapOf("baz" to "bar")
    }
}

class ChainTest {
    @Test
    fun testCorrectCall() {
        val chain = FakeChain()
        val output = chain.invoke(mapOf("foo" to "bar"))
        assertEquals(mapOf("foo" to "bar", "bar" to "baz"), output)
    }

    @Test fun testRunArgs() {
        val chain = FakeChain(theInputKeys = listOf("foo", "bar"))
        val output = chain.run(mapOf("foo" to "bar", "bar" to "foo"))
        assertEquals("baz", output)
    }
}