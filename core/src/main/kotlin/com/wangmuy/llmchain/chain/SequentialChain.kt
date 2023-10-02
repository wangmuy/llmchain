package com.wangmuy.llmchain.chain

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.BaseMemory

class SequentialChain(
    var chains: List<Chain>,
    var inputVariables: List<String> = chains.first().inputKeys()!!,
    var outputVariables: List<String> = chains.last().outputKeys(),
    var returnAll: Boolean = false,
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): Chain(memory, callbackManager, verbose) {

    init {
        setAllCallbacks(callbackManager)
    }

    private fun setAllCallbacks(callbackManager: BaseCallbackManager?) {
        super.callbackManager = callbackManager
        chains.forEach { it.callbackManager = callbackManager }
    }

    override var callbackManager: BaseCallbackManager?
        get() = super.callbackManager
        set(value) {
            setAllCallbacks(value)
        }

    override fun inputKeys(): List<String>? {
        return inputVariables
    }

    override fun outputKeys(): List<String> {
        return outputVariables
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        val knownValues = mutableMapOf<String, Any>().apply { putAll(inputs!!) }
        chains.forEachIndexed { i, chain ->
            val outputs = chain.invoke(knownValues)
            knownValues.putAll(outputs)
        }
        return knownValues.filter { it.key in outputVariables }
    }
}

class SimpleSequentialChain(
    var chains: List<Chain>,
    var stripOutputs: Boolean = false,
    var inputKey: String = chains.first().inputKeys()!!.first(),
    var outputKey: String = chains.last().outputKeys().first(),
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): Chain(memory, callbackManager, verbose) {

    init {
        setAllCallbacks(callbackManager)
    }

    private fun setAllCallbacks(callbackManager: BaseCallbackManager?) {
        super.callbackManager = callbackManager
        chains.forEach { it.callbackManager = callbackManager }
    }

    override var callbackManager: BaseCallbackManager?
        get() = super.callbackManager
        set(value) {
            setAllCallbacks(value)
        }

    override fun inputKeys(): List<String>? {
        return listOf(inputKey)
    }

    override fun outputKeys(): List<String> {
        return listOf(outputKey)
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        var input = inputs!!.filter { it.key == inputKey }
        chains.forEachIndexed { i, chain->
            input = chain.invoke(input)
            if (stripOutputs) {
                input = input.mapValues { it.toString().trim() }
            }
        }
        return mapOf(outputKey to input[outputKey]!!)
    }
}