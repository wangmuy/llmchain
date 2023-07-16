package com.wangmuy.llmchain

import com.wangmuy.llmchain.chain.LLMMathChain
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import org.junit.Assert.assertEquals
import org.junit.Test

class LLMMathTest {
    @Test fun llmMathChainTest() {
        val llm = OpenAIChat("sk-")
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 400
        val chain = LLMMathChain(llm)
        val ret = chain.invoke(mapOf("question" to "What is 5 + 7 - 3 * 2"))
        println("invoke result=\n$ret")
        assertEquals("6", ret[chain.outputKeys()[0]])
    }
}