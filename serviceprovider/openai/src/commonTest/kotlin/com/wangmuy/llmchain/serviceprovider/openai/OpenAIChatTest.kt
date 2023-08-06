package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.llm.BaseLLM
import kotlinx.datetime.Clock

class OpenAIChatTest {
    fun testOpenAIChat() {
        var proxy: String? = null
        proxy = "socks5://127.0.0.1:1090"
        val baseUrl = "https://api.openai.com/v1/" // FastChat http://localhost:8000/v1/
        val llm = OpenAIChat(apiKey = "sk-", baseUrl = baseUrl, timeoutMillis = 60000, proxy = proxy)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = Clock.System.now().toEpochMilliseconds()
        val output = llm.invoke("Say foo:", null)
        val costMillis = Clock.System.now().toEpochMilliseconds() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}