package com.wangmuy.llmchain.callback

import com.wangmuy.llmchain.schema.AgentAction
import com.wangmuy.llmchain.schema.AgentFinish
import com.wangmuy.llmchain.schema.LLMResult

interface BaseCallbackHandler {
    fun alwaysVerbose(): Boolean = false
    fun ignoreLLM(): Boolean = false
    fun ignoreChain(): Boolean = false
    fun ignoreAgent(): Boolean = false
    fun onLLMStart(serialized: Map<String, Any>, prompts: List<String>, verbose: Boolean = false)
    fun onLLMNewToken(token: String, verbose: Boolean = false)
    fun onLLMEnd(response: LLMResult, verbose: Boolean = false)
    fun onLLMError(error: Throwable, verbose: Boolean = false)
    fun onChainStart(serialized: Map<String, Any>, inputs: Map<String, Any>?, verbose: Boolean = false)
    fun onChainEnd(outputs: Map<String, Any>, verbose: Boolean = false)
    fun onChainError(error: Throwable, verbose: Boolean = false)
    fun onToolStart(serialized: Map<String, Any>, inputStr: String, verbose: Boolean = false)
    fun onToolEnd(output: String, verbose: Boolean = false)
    fun onToolError(error: Throwable, verbose: Boolean = false)
    fun onText(text: String, verbose: Boolean = false)
    fun onAgentAction(action: AgentAction, verbose: Boolean = false)
    fun onAgentFinish(finish: AgentFinish, verbose: Boolean = false)
}