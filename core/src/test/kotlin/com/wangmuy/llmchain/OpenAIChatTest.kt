package com.wangmuy.llmchain

import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import org.junit.Test
import java.net.Proxy

class OpenAIChatTest {
    @Test fun testOpenAIChat() {
        var proxy: Proxy? = null
//        proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", 1090))
        val baseUrl = "https://api.openai.com/" // FastChat http://localhost:8000/v1/
        val llm = OpenAIChat(apiKey = "sk-", baseUrl = baseUrl, timeoutMillis = 60000, proxy = proxy)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = System.currentTimeMillis()
        val output = llm.invoke("Say foo:", null)
        val costMillis = System.currentTimeMillis() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}