package com.wangmuy.llmchain.serviceprovider.openai

import kotlin.test.Test

class QuickStartJvmTest {
    @Test fun testLLMs() {
        val test = Quickstart()
        test.testLLMs()
    }

    @Test fun testChains() {
        val test = Quickstart()
        test.testChains()
    }

    @Test fun testAgents() {
        val test = Quickstart()
        test.testAgents()
    }

    @Test fun testMemory() {
        val test = Quickstart()
        test.testMemory()
    }
}