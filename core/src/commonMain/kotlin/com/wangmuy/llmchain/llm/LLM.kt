package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.Generation
import com.wangmuy.llmchain.schema.LLMResult

abstract class LLM(
    verbose: Boolean = false,
    callbackManager: BaseCallbackManager? = null
): BaseLLM(verbose, callbackManager) {
    protected abstract fun onInvoke(prompt: String, stop: List<String>?, inputList: List<Map<String, Any>> = emptyList()): String // _call

    override fun onGenerate(prompts: List<String>, stop: List<String>?, inputList: List<Map<String, Any>>): LLMResult {
        val generations = mutableListOf<List<Generation>>()
        for (prompt in prompts) {
            val text = onInvoke(prompt, stop, inputList)
            generations.add(listOf(Generation(text)))
        }
        return LLMResult(generations)
    }
}