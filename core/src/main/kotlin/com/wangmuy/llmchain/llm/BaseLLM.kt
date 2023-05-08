package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.schema.PromptValue

abstract class BaseLLM @JvmOverloads constructor(
    val verbose: Boolean = false,
    callbackManager: BaseCallbackManager? = null
): BaseLanguageModel(callbackManager), (String, List<String>?) -> String {
    private var cache: Boolean = false

    override fun generatePrompt(prompts: List<PromptValue>, stop: List<String>?): LLMResult {
        val promptStrings = prompts.map { it.asString() }.toCollection(mutableListOf())
        return generate(promptStrings, stop)
    }

    abstract fun onGenerate(prompts: List<String>, stop: List<String>?): LLMResult

    fun generate(prompts: List<String>, stop: List<String>?): LLMResult {
        if (!cache) {
            callbackManager?.onLLMStart(mapOf("name" to javaClass.name), prompts, verbose)
            try {
                val output = onGenerate(prompts, stop)
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

    override fun invoke(prompt: String, stop: List<String>?): String {
        return generate(listOf(prompt), stop).generations[0][0].text
    }
}