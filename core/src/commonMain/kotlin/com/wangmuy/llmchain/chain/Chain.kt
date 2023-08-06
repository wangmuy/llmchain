package com.wangmuy.llmchain.chain

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.BaseMemory
import kotlin.jvm.JvmOverloads

abstract class Chain @JvmOverloads constructor(
    var memory: BaseMemory? = null,
    var callbackManager: BaseCallbackManager? = null,
    val verbose: Boolean = false
): ChainInvoker {
    companion object {
        const val KEY_NAME = "name"
    }

    abstract fun inputKeys(): List<String>?
    abstract fun outputKeys(): List<String>
    abstract fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> // _call
    override fun invoke(inputs: Map<String, Any>?): Map<String, Any> {// __call__
        val inputsPrep = prepInputs(inputs)
        callbackManager?.onChainStart(mapOf(KEY_NAME to this::class.simpleName as Any), inputsPrep, verbose)
        try {
            val outputs = onInvoke(inputsPrep)
            callbackManager?.onChainEnd(outputs, verbose)
            return prepOutputs(inputsPrep, outputs)
        } catch (e: Exception) {
            callbackManager?.onChainError(e, verbose)
            throw e
        }
    }

    fun prepOutputs(
        inputs: Map<String, Any>,
        outputs: Map<String, Any>,
        returnOnlyOutputs: Boolean = false): Map<String, Any> {
        memory?.saveContext(inputs, outputs.mapValues { it.value.toString() })
        return if (returnOnlyOutputs)
            outputs
        else {
            inputs.toMutableMap().also { it.putAll(outputs) }
        }
    }

    fun prepInputs(inputs: Map<String, Any>?): Map<String, Any> {
        val inputsNotNull = inputs ?: emptyMap()
        val externalContext = memory?.loadMemoryVariables(inputsNotNull) ?: mutableMapOf()
        return inputsNotNull.toMutableMap().also { it.putAll(externalContext) }
    }

    open fun apply(inputList: List<Map<String, Any>>): List<Map<String, Any>> {
        return inputList.map { invoke(it) }
    }

    fun run(args: Map<String, Any>?): String {
        val outputKeys = outputKeys()
        if (outputKeys.size != 1) {
            throw IllegalArgumentException("`run` not supported when there is not exactly " +
                    "one output key. Got $outputKeys")
        }
        return invoke(args)[outputKeys[0]]!!.toString()
    }
}