package com.wangmuy.llmchain

import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.llm.LLM
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.schema.BaseOutputParser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeOutputParser: BaseOutputParser<String> {
    override fun parse(text: String): String {
        return text.split(Regex("\\s+")).joinToString(" ")
    }

    override fun getFormatInstructions(): String {
        throw UnsupportedOperationException("not implemented")
    }

}

class FakeLLM(private val queries: Map<String, String>? = null): LLM() {
    override fun onInvoke(prompt: String, stop: List<String>?, inputList: List<Map<String, Any>>): String {
        if (queries != null) {
            return queries[prompt]!!
        }
        return if (stop == null) {
            "foo"
        } else {
            "bar"
        }
    }
}

class LLMTest {
    private lateinit var chain: LLMChain

    @Before fun before() {
        val prompt = PromptTemplate(listOf("bar"), "This is a {bar}:")
        chain = LLMChain(prompt, FakeLLM(), outputKey = "text1")
    }

    @Test fun testValidCall() {
        val output = chain.invoke(mapOf("bar" to "baz"))
        assertEquals(mapOf("bar" to "baz", "text1" to "foo"), output)
    }

    @Test fun testPredictMethod() {
        val output = chain.predict(mapOf("bar" to "baz"))
        assertEquals("foo", output)
    }

    @Test fun testPredictAndParse() {
        val prompt = PromptTemplate(inputVariables = listOf("foo"), template = "{foo}", outputParser = FakeOutputParser() as BaseOutputParser<Any>)
        val llm = FakeLLM(mapOf("foo" to "foo  \n  bar"))
        val chain = LLMChain(prompt, llm)
        val output = chain.predictAndParse<String>(mapOf("foo" to "foo"))
        assertEquals("foo bar", output)
    }
}