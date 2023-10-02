package com.wangmuy.llmchain.chain

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.prompt.BasePromptTemplate
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.schema.BaseMemory
import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.schema.PromptValue
import java.util.Collections

open class LLMChain @JvmOverloads constructor(
    val prompt: BasePromptTemplate,
    val llm: BaseLanguageModel,
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    val outputKey: String = "text",
    verbose: Boolean = false,
): Chain(memory, callbackManager, verbose) {
    private var mOutputKeys: String = "text"

    init {
        if (callbackManager != null && llm.callbackManager == null) {
            llm.callbackManager = callbackManager
        }
    }

    override var callbackManager: BaseCallbackManager?
        get() = super.callbackManager
        set(value) {
            super.callbackManager = value
            llm.callbackManager = value
        }

    override fun inputKeys(): List<String>? {
        return prompt.inputVariables
    }

    override fun outputKeys(): List<String> {
        return Collections.singletonList(outputKey)
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        val inputsNotNull = inputs ?: Collections.emptyMap()
        return apply(Collections.singletonList(inputsNotNull))[0]
    }

    fun generate(inputList: List<Map<String, Any>>): LLMResult {
        val prompts = mutableListOf<PromptValue>()
        var stop: MutableList<String>? = mutableListOf()
        prepPrompts(inputList, prompts, stop!!)
        if (stop.isEmpty()) {
            stop = null
        }
        return llm.generatePrompt(prompts, stop, inputList)
    }

    fun prepPrompts(
        inputList: List<Map<String, Any>>,
        promptsRet: MutableList<PromptValue>,
        stopRet: MutableList<String>) {
        val stop = if (inputList[0].containsKey("stop")) inputList[0]["stop"] as List<String> else null
        for (inputs in inputList) {
            val selectedInputs = inputs.filter { this.prompt.inputVariables.contains(it.key) }
            val prompt = this.prompt.formatPrompt(selectedInputs)
            callbackManager?.onText("Prompt after formatting:\n$prompt", verbose)
            if (inputs.containsKey("stop") && inputs["stop"] != stop) {
                throw IllegalArgumentException("If `stop` is present in any inputs, should be present in all.")
            }
            promptsRet.add(prompt)
        }
        if (stop != null) {
            stopRet.addAll(stop)
        }
    }

    override fun apply(inputList: List<Map<String, Any>>): List<Map<String, String>> {
        val response = generate(inputList)
        // create_outputs
        val outputs = mutableListOf<Map<String, String>>()
        for (generation in response.generations) {
            outputs.add(mapOf(outputKey to generation[0].text))
        }
        return outputs
    }

    fun <T> applyAndParse(inputList: List<Map<String, Any>>): List<T> {
        val result = apply(inputList)
        val parser = prompt.outputParser as BaseOutputParser<T>
        return result.map { parser.parse(it[outputKey]!!) }
    }

    fun predict(inputs: Map<String, Any>): String {
        return invoke(inputs)[outputKey]!!.toString()
    }

    fun <T> predictAndParse(inputs: Map<String, Any>): T {
        val result = predict(inputs)
        val parser = prompt.outputParser as BaseOutputParser<T>
        return parser.parse(result)
    }
}