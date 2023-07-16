package com.wangmuy.llmchain.serviceprovider.gpt4all

import com.hexadevlabs.gpt4all.LLModel
import java.nio.file.Path

class ModelHolder(val modelPath: String): AutoCloseable {
    private var closed: Boolean = false

    private val lazyModel = lazy {
        LLModel(Path.of(modelPath))
    }

    val model: LLModel
        get() = if (closed) {
            throw IllegalStateException("model is closed, path=$modelPath")
        } else lazyModel.value

    override fun close() {
        if (lazyModel.isInitialized()) {
            lazyModel.value.close()
            closed = true
        }
    }
}