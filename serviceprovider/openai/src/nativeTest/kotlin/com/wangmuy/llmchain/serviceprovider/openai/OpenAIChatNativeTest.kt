package com.wangmuy.llmchain.serviceprovider.openai

import kotlin.test.Test

class OpenAIChatNativeTest {
    @Test fun testOpenAIChat() {
        try {
            val test = OpenAIChatTest()
            test.testOpenAIChat()
        } catch (e: Exception) {
            println(e.stackTraceToString())
        }
    }
}