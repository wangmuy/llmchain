package com.wangmuy.llmchain.callback

import com.wangmuy.llmchain.schema.AgentAction
import com.wangmuy.llmchain.schema.AgentFinish
import com.wangmuy.llmchain.schema.LLMResult

open class DefaultCallbackHandler: BaseCallbackHandler {
    override fun onLLMStart(serialized: Map<String, Any>, prompts: List<String>, verbose: Boolean) {
    }

    override fun onLLMNewToken(token: String, verbose: Boolean) {
    }

    override fun onLLMEnd(response: LLMResult, verbose: Boolean) {
    }

    override fun onLLMError(error: Throwable, verbose: Boolean) {
    }

    override fun onChainStart(
        serialized: Map<String, Any>,
        inputs: Map<String, Any>?,
        verbose: Boolean
    ) {
    }

    override fun onChainEnd(outputs: Map<String, Any>, verbose: Boolean) {
    }

    override fun onChainError(error: Throwable, verbose: Boolean) {
    }

    override fun onToolStart(serialized: Map<String, Any>, inputStr: String, verbose: Boolean) {
    }

    override fun onToolEnd(output: String, verbose: Boolean) {
    }

    override fun onToolError(error: Throwable, verbose: Boolean) {
    }

    override fun onText(text: String, verbose: Boolean) {
    }

    override fun onAgentAction(action: AgentAction, verbose: Boolean) {
    }

    override fun onAgentFinish(finish: AgentFinish, verbose: Boolean) {
    }
}