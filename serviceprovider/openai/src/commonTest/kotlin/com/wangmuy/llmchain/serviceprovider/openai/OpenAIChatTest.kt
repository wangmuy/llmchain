package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.llm.BaseLLM
import kotlinx.datetime.Clock
import kotlin.test.Test

// use specific platform to run test(jvmMain/nativeMain/jsMain)
class OpenAIChatTest {
    @Test fun testOpenAIChat() {
        val llm = OpenAIChat(
            apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = Clock.System.now().toEpochMilliseconds()
        val output = llm.invoke("Say foo:", null)
        val costMillis = Clock.System.now().toEpochMilliseconds() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}