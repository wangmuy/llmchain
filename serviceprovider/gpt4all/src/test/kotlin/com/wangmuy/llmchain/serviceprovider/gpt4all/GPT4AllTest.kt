package com.wangmuy.llmchain.serviceprovider.gpt4all

import com.hexadevlabs.gpt4all.LLModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Path

class GPT4AllTest {
    private lateinit var model: LLModel

    @Before
    fun before() {
//        LLModel.LIBRARY_SEARCH_PATH = "E:\\project\\ai\\gpt4all-libs"
        val modelFilePath = "E:\\project\\ai\\gpt4all-models\\ggml-gpt4all-j-v1.3-groovy.bin"
        model = LLModel(Path.of(modelFilePath))
    }

    @After
    fun after() {
        model.close()
    }

    @Test
    fun testPrompt() {
        val config = LLModel.config()
            .withNPredict(4096)
            .build()
        val prompt = "### Human:\\nWhat is the meaning of life\\n### Assistant:"
        val output = model.generate(prompt, config, false)
        println("output=$output")
    }
}