package com.wangmuy.llmchain

import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import org.junit.Test

class OpenAIChatTest {
    @Test fun testOpenAIChat() {
        val llm = OpenAIChat(apiKey = "sk-")
        llm.invocationParams[OpenAIChat.REQ_MAX_TOKENS] = 40
        val output = llm.invoke("Say foo:", null)
        println("output=$output")
    }
}