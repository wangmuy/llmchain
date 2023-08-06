package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.tool.BaseTool

class Factory {
    companion object {
        const val AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION = "zero-shot-react-description"

        fun initializeAgent(
            tools: List<BaseTool>,
            llm: BaseLanguageModel,
            agentType: String,
            callbackManager: BaseCallbackManager? = null,
            args: Map<String, Any>? = null
        ): AgentExecutor {
            val agent = getAgent(llm, tools, agentType, callbackManager, args)
            return AgentExecutor(agent, tools, callbackManager)
        }

        private fun getAgent(
            llm: BaseLanguageModel,
            tools: List<BaseTool>,
            agentType: String,
            callbackManager: BaseCallbackManager?,
            args: Map<String, Any>?): Agent {
            when (agentType) {
                AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION -> {
                    return ZeroShotAgent.Builder().llm(llm).tools(tools).also {
                        if (callbackManager != null) {
                            it.callbackManager(callbackManager)
                        }
                        if (args != null) {
                            it.args(args)
                        }
                    }.build()
                }
                else -> {
                    throw NotImplementedError("Not implemented agentType=$agentType")
                }
            }
        }
    }
}