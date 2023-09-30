package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.schema.PromptValue

typealias LLMInvoke = (String, List<String>?, List<Map<String, Any>>) -> String

abstract class BaseLLM @JvmOverloads constructor(
    val verbose: Boolean = false,
    callbackManager: BaseCallbackManager? = null
): BaseLanguageModel(callbackManager), LLMInvoke {
    companion object {
        const val REQ_MODEL_NAME = "model_name"
        const val REQ_USER_NAME = "user_name"
        const val REQ_TEMPERATURE = "temperature"
        const val REQ_MAX_TOKENS = "max_tokens"
        const val REQ_TOP_P = "top_p"
        const val REQ_FREQUENCY_PENALTY = "frequency_penalty"
        const val REQ_PRESENCE_PENALTY = "presence_penalty"
        const val REQ_N = "n"
        //        const val REQ_BEST_OF = "best_of"
        const val REQ_LOGIT_BIAS = "logit_bias"
        const val RSP_TOKEN_USAGE = "token_usage"
        const val RSP_MESSAGE = "message"
    }

    private var cache: Boolean = false

    override fun generatePrompt(
        prompts: List<PromptValue>,
        stop: List<String>?,
        inputList: List<Map<String, Any>>): LLMResult {
        val promptStrings = prompts.map { it.asString() }.toCollection(mutableListOf())
        return generate(promptStrings, stop, inputList)
    }

    abstract fun onGenerate(prompts: List<String>, stop: List<String>?, inputList: List<Map<String, Any>>): LLMResult

    fun generate(
        prompts: List<String>,
        stop: List<String>?,
        inputList: List<Map<String, Any>> = emptyList()
    ): LLMResult {
        if (!cache) {
            callbackManager?.onLLMStart(mapOf("name" to javaClass.name), prompts, verbose)
            try {
                val output = onGenerate(prompts, stop, inputList)
                callbackManager?.onLLMEnd(output, verbose)
                return output
            } catch (e: Exception) {
                callbackManager?.onLLMError(e, verbose)
                throw e
            }
        }
        // todo
        throw IllegalStateException("todo")
    }

    fun invoke(prompt: String, stop: List<String>?): String {
        return invoke(prompt, stop, emptyList())
    }

    override fun invoke(prompt: String, stop: List<String>?, inputList: List<Map<String, Any>>): String {
        return generate(listOf(prompt), stop, inputList).generations[0][0].text
    }
}