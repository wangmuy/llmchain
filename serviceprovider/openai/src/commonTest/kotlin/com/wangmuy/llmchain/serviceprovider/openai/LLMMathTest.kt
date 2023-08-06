package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.chain.LLMMathChain
import com.wangmuy.llmchain.llm.BaseLLM
import kotlin.test.Test
import kotlin.test.assertEquals

// use specific platform to run test(jvmMain/nativeMain/jsMain)
class LLMMathTest {
    @Test fun llmMathChainTest() {
        val llm = OpenAIChat(
            apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 400
        val chain = LLMMathChain(llm)
        val ret = chain.invoke(mapOf("question" to "What is 5 + 7 - 3 * 2"))
        println("invoke result=\n$ret")
        assertEquals("6", ret[chain.outputKeys()[0]])
    }
}