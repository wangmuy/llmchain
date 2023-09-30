package com.wangmuy.llmchain

import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Proxy

class OpenAIChatTest {
    companion object {
        const val BASEURL = "https://api.openai.com/v1/" // Fastchat http://localhost:8000/v1/
        val APIKEY = "src/test/resources/private.properties".filePathAsProperties().getProperty("APIKEY")
        val PROXY: String? = "socks5://127.0.0.1:1090"
        val TIMEOUT_MILLIS: Long = 60000
    }

    @Test fun testOpenAIChat() {
        val llm = OpenAIChat(apiKey = APIKEY, baseUrl = BASEURL, timeoutMillis = TIMEOUT_MILLIS, proxy = PROXY)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"//"gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = System.currentTimeMillis()
        val output = llm.invoke("Say foo:", null)
        val costMillis = System.currentTimeMillis() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}