package com.wangmuy.llmchain.serviceprovider.gpt4all

import com.hexadevlabs.gpt4all.LLModel
import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.llm.LLM

// https://github.com/nomic-ai/gpt4all/tree/main/gpt4all-bindings/java
class GPT4AllCompletion @JvmOverloads constructor(
    val invocationParams: MutableMap<String, Any> = DEFAULT_PARAMS,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): LLM(verbose, callbackManager) {
    companion object {
        const val REQ_MODEL_PATH = "model_path"
        const val REQ_STREAM_TO_STDOUT = "stream_to_stdout"
        private val DEFAULT_PARAMS = mutableMapOf<String, Any>(
            REQ_MODEL_PATH to "./ggml-gpt4all-j-v1.3-groovy.bin",
            REQ_MAX_TOKENS to 4096,
            REQ_N to 1,
            REQ_STREAM_TO_STDOUT to false
        )
    }

    private var modelHolder: ModelHolder

    init {
        DEFAULT_PARAMS.filterNot { it.key in invocationParams }.forEach {
            invocationParams[it.key] = it.value
        }
        modelHolder = ModelHolder(invocationParams[REQ_MODEL_PATH] as String)
    }

    fun setModel(modelPath: String?) {
        if (modelPath != modelHolder.modelPath) {
            modelHolder.close()
            if (modelPath != null) {
                modelHolder = ModelHolder(modelPath)
            }
        }
    }

    override fun onInvoke(prompt: String, stop: List<String>?, inputList: List<Map<String, Any>>): String {
        val configBuilder = LLModel.config()
            .withNPredict(invocationParams[REQ_MAX_TOKENS] as Int)
            .withTopK(invocationParams[REQ_N] as Int)
        val config = configBuilder.build()
        val willStream = invocationParams[REQ_STREAM_TO_STDOUT] as Boolean
        return modelHolder.model.generate(prompt, config, willStream)
    }
}