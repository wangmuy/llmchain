package com.wangmuy.llmchain.agent.router

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.outputparser.JsonOutputParser
import com.wangmuy.llmchain.schema.BaseMemory
import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.utils.toStringWithoutQuotes
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LLMRouterChain(
    val llmChain: LLMChain,
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): RouterChain(memory, callbackManager, verbose) {

    init {
        llmChain.callbackManager = callbackManager
    }

    override var callbackManager: BaseCallbackManager?
        get() = super.callbackManager
        set(value) {
            super.callbackManager = value
            llmChain.callbackManager = value
        }

    override fun inputKeys(): List<String>? {
        return llmChain.inputKeys()
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        return llmChain.predictAndParse(inputs!!)
    }
}

class RouterOutputParser(
    val defaultDestination: String = "DEFAULT",
    val nextInputsInnerKey: String = "input"
): BaseOutputParser<Map<String, Any>> {

    override fun parse(text: String): Map<String, Any> {
        val expectedKeys = listOf(RouterChain.KEY_DESTINATION, RouterChain.KEY_NEXT_INPUTS)
        val parsed = JsonOutputParser.parseAndCheckJsonMarkdown(text, expectedKeys)
        var destination = parsed.jsonObject[RouterChain.KEY_DESTINATION]!!.toStringWithoutQuotes()
        destination = if (destination.trim().lowercase() == defaultDestination.lowercase()) {
            ""
        } else {
            destination.trim()
        }
        val oldNextInputs = parsed.jsonObject[RouterChain.KEY_NEXT_INPUTS]!!.jsonPrimitive.toStringWithoutQuotes()
        return mapOf(
            RouterChain.KEY_DESTINATION to destination,
            RouterChain.KEY_NEXT_INPUTS to mapOf(nextInputsInnerKey to oldNextInputs)
        )
    }
}