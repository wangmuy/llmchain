package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.CallbackManager
import com.wangmuy.llmchain.schema.Generation
import com.wangmuy.llmchain.schema.LLMResult

abstract class LLM(
    verbose: Boolean = false,
    callbackManager: CallbackManager? = null
): BaseLLM(verbose, callbackManager) {
    protected abstract fun onInvoke(prompt: String, stop: List<String>?): String // _call

    override fun onGenerate(prompts: List<String>, stop: List<String>?): LLMResult {
        val generations = mutableListOf<List<Generation>>()
        for (prompt in prompts) {
            val text = onInvoke(prompt, stop)
            generations.add(listOf(Generation(text)))
        }
        return LLMResult(generations)
    }
}