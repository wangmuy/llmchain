package com.wangmuy.llmchain.callback

import com.wangmuy.llmchain.schema.AgentAction
import com.wangmuy.llmchain.schema.AgentFinish
import com.wangmuy.llmchain.schema.LLMResult

class CallbackManager(
        private var handlers: MutableList<BaseCallbackHandler>
): BaseCallbackManager() {
    override fun addHandler(callback: BaseCallbackHandler) {
        handlers.add(callback)
    }

    override fun removeHandler(handler: BaseCallbackHandler) {
        handlers.remove(handler)
    }

    override fun setHandlers(handlers: List<BaseCallbackHandler>) {
        this.handlers = mutableListOf()
        this.handlers.addAll(handlers)
    }

    override fun onLLMStart(serialized: Map<String, Any>, prompts: List<String>, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreLLM() && (verbose || h.alwaysVerbose())) {
                h.onLLMStart(serialized, prompts, verbose)
            }
        }
    }

    override fun onLLMNewToken(token: String, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreLLM() && (verbose || h.alwaysVerbose())) {
                h.onLLMNewToken(token, verbose)
            }
        }
    }

    override fun onLLMEnd(response: LLMResult, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreLLM() && (verbose || h.alwaysVerbose())) {
                h.onLLMEnd(response, verbose)
            }
        }
    }

    override fun onLLMError(error: Throwable, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreLLM() && (verbose || h.alwaysVerbose())) {
                h.onLLMError(error, verbose)
            }
        }
    }

    override fun onChainStart(serialized: Map<String, Any>, inputs: Map<String, Any>?, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreChain() && (verbose || h.alwaysVerbose())) {
                h.onChainStart(serialized, inputs, verbose)
            }
        }
    }

    override fun onChainEnd(outputs: Map<String, Any>, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreChain() && (verbose || h.alwaysVerbose())) {
                h.onChainEnd(outputs, verbose)
            }
        }
    }

    override fun onChainError(error: Throwable, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreChain() && (verbose || h.alwaysVerbose())) {
                h.onChainError(error, verbose)
            }
        }
    }

    override fun onToolStart(serialized: Map<String, Any>, inputStr: String, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreAgent() && (verbose || h.alwaysVerbose())) {
                h.onToolStart(serialized, inputStr, verbose)
            }
        }
    }

    override fun onToolEnd(output: String, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreAgent() && (verbose || h.alwaysVerbose())) {
                h.onToolEnd(output, verbose)
            }
        }
    }

    override fun onToolError(error: Throwable, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreAgent() && (verbose || h.alwaysVerbose())) {
                h.onToolError(error, verbose)
            }
        }
    }

    override fun onText(text: String, verbose: Boolean) {
        for (h in handlers) {
            if (verbose || h.alwaysVerbose()) {
                h.onText(text, verbose)
            }
        }
    }

    override fun onAgentAction(action: AgentAction, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreAgent() && (verbose || h.alwaysVerbose())) {
                h.onAgentAction(action, verbose)
            }
        }
    }

    override fun onAgentFinish(finish: AgentFinish, verbose: Boolean) {
        for (h in handlers) {
            if (!h.ignoreAgent() && (verbose || h.alwaysVerbose())) {
                h.onAgentFinish(finish, verbose)
            }
        }
    }
}