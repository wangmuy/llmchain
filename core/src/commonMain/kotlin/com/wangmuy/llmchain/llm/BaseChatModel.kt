package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.*
import kotlin.jvm.JvmOverloads

abstract class BaseChatModel @JvmOverloads constructor(
    val verbose: Boolean = false,
    callbackManager: BaseCallbackManager? = null
): BaseLanguageModel(callbackManager), BaseChatInvoker {
    protected open fun combineLLMOutputs(llmOutputs: List<Map<String, String>>): Map<String, String> {
        return emptyMap()
    }

    fun generate(messages: List<List<BaseMessage>>, stop: List<String>?): LLMResult {
        val results = messages.map { onGenerate(it, stop) }
        val llmOutput = combineLLMOutputs(results.map { it.llmOutput!! })
        val generations = results.map { it.generations }
        return LLMResult(generations, llmOutput)
    }

    override fun generatePrompt(
        prompts: List<PromptValue>,
        stop: List<String>?,
        inputList: List<Map<String, Any>>): LLMResult {
        val promptMessages = prompts.map { it.asMessage() }
        val promptStrings = prompts.map { it.asString() }
        callbackManager?.onLLMStart(mapOf("name" to this::class.simpleName as Any), promptStrings, verbose)
        try {
            val output = generate(promptMessages, stop)
            callbackManager?.onLLMEnd(output, verbose)
            return output
        } catch (e: Exception) {
            callbackManager?.onLLMError(e, verbose)
            throw e
        }
    }

    protected abstract fun onGenerate(messages: List<BaseMessage>, stop: List<String>?): ChatResult

    override fun invoke(messages: List<BaseMessage>, stop: List<String>?): BaseMessage {
        return onGenerate(messages, stop).generations[0].message
    }
}