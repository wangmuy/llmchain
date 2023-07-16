package com.wangmuy.llmchain.serviceprovider.gpt4all

import com.wangmuy.llmchain.serviceprovider.gpt4all.GPT4AllCompletion.Companion.REQ_MODEL_PATH
import org.junit.Test

class GPT4AllCompletionTest {
    @Test
    fun testGPT4AllCompletion() {
        val modelPath = "E:\\project\\ai\\gpt4all-models\\ggml-gpt4all-j-v1.3-groovy.bin"
        val llm = GPT4AllCompletion(mutableMapOf(REQ_MODEL_PATH to modelPath))
        val text = "### Human: What would be a good company name for a company that makes colorful socks?\n### Assistant:"
        val output = llm.invoke(text, null)
        println("output=\n$output")
    }
}