package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.prompt.fStringFormat
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.tool.BaseTool
import java.util.regex.Matcher
import java.util.regex.Pattern

open class ZeroShotAgent(
    llmChain: LLMChain,
    allowedTools: List<String>,
    val toolRunArgs: MutableMap<String, Any> = mutableMapOf()
): Agent(llmChain, allowedTools) {
    companion object {
        private const val FINAL_ANSWER_ACTION = "Final Answer:"

        private const val PROMPT_PREFIX = "Answer the following questions as best you can. You have access to the following tools:"
        private const val PROMPT_FORMAT_INSTRUCTIONS = """Use the following format:

Question: the input question you must answer
Thought: you should always think about what to do
Action: the action to take, should be one of [{tool_names}]
Action Input: the input to the action
Observation: the result of the action
... (this Thought/Action/Action Input/Observation can repeat N times)
Thought: I now know the final answer
Final Answer: the final answer to the original input question"""
        private const val PROMPT_SUFFIX = """Begin!

Question: {input}
Thought:{agent_scratchpad}"""

        private val PATTERN_ACTION = Pattern.compile("Action\\s*\\d*: (.*?)")
        private val PATTERN_ACTION_INPUT = Pattern.compile("Action Input:\\s*(.*)")
    }

    override fun observationPrefix(): String {
        return "Observation: "
    }

    override fun llmPrefix(): String {
        return "Thought:"
    }

    override fun toolRunLoggingArgs(): Map<String, Any> {
        for ((k,v) in super.toolRunLoggingArgs()) {
            toolRunArgs.putIfAbsent(k, v)
        }
        return toolRunArgs
    }

    override fun extractToolAndInput(text: String): Pair<String, String>? {
        val llmOutput = text
        if (llmOutput.contains(FINAL_ANSWER_ACTION)) {
            return Pair("Final Answer", llmOutput.split(FINAL_ANSWER_ACTION).last().trim())
        }
        val lines = llmOutput.split("\n")
        var action: String? = null
        var actionInput: String? = null
        for (line in lines) {
            var matcher: Matcher?
            matcher = if (action == null) PATTERN_ACTION.matcher(line) else null
            if (matcher != null && matcher.matches()) {
                action = matcher.group(1)?.trim{ c -> !c.isLetterOrDigit() && c != '_'}
                continue
            }
            matcher = if (actionInput == null) PATTERN_ACTION_INPUT.matcher(line) else null
            if (matcher != null && matcher.matches()) {
                actionInput = matcher.group(1)?.trim{ c -> c.isWhitespace() || c == '"'}
                continue
            }
        }
        if (action == null || actionInput == null) {
            throw IllegalStateException("Could not parse LLM output: $llmOutput")
        }
        return Pair(action, actionInput)
    }

    open class PromptBuilder(): Agent.PromptBuilder<PromptBuilder, PromptTemplate>() {
        private var tools: List<BaseTool>? = null
        private var prefix: String = PROMPT_PREFIX
        private var suffix: String = PROMPT_SUFFIX
        private var formatInstructions: String = PROMPT_FORMAT_INSTRUCTIONS
        private var inputVariables: List<String>? = null

        companion object {
            const val KEY_PREFIX = "prefix"
            const val KEY_SUFFIX = "suffix"
            const val KEY_FORMAT_INSTRUCTIONS = "formatInstructions"
            const val KEY_INPUT_VARIABLES = "inputVariables"
        }

        override fun self(): PromptBuilder {
            return this
        }

        fun tools(tools: List<BaseTool>): PromptBuilder {
            this.tools = tools
            return this
        }

        fun prefix(prefix: String): PromptBuilder {
            this.prefix = prefix
            return this
        }

        fun suffix(suffix: String): PromptBuilder {
            this.suffix = suffix
            return this
        }

        fun formatInstructions(fi: String): PromptBuilder {
            this.formatInstructions = fi
            return this
        }

        fun inputVariables(iv: List<String>): PromptBuilder {
            this.inputVariables = iv
            return this
        }

        override fun build(): PromptTemplate {
            val toolStrings = tools?.joinToString("\n") { "${it.name}: ${it.description}" } ?: ""
            val toolNames = tools?.joinToString(", ") { it.name } ?: ""
            val formatInstructions = this.formatInstructions.fStringFormat(mapOf("tool_names" to toolNames), false)
            val template = listOf(prefix, toolStrings, formatInstructions, suffix).joinToString("\n\n")
            if (inputVariables == null) {
                inputVariables = listOf("input", AGENT_SCRATCHPAD)
            }
            return PromptTemplate(inputVariables!!, template)
        }

    }

    open class Builder(): Agent.Builder<Builder, ZeroShotAgent>() {
        companion object {
            @JvmStatic
            fun applyParams(args: Map<String, Any>?, promptBuilder: PromptBuilder) {
                if (args != null) {
                    args[PromptBuilder.KEY_PREFIX]?.also { promptBuilder.prefix(it as String) }
                    args[PromptBuilder.KEY_SUFFIX]?.also { promptBuilder.suffix(it as String) }
                    args[PromptBuilder.KEY_FORMAT_INSTRUCTIONS]?.also { promptBuilder.formatInstructions(it as String) }
                    args[PromptBuilder.KEY_INPUT_VARIABLES]?.also { promptBuilder.inputVariables(it as List<String>) }
                }
            }
        }

        var llm: BaseLanguageModel? = null
        var tools: List<BaseTool>? = null
        var callbackManager: BaseCallbackManager? = null
        var args: Map<String, Any>? = null
        val promptBuilder: PromptBuilder = PromptBuilder()

        override fun self(): Builder {
            return this
        }

        fun promptBuilder(): PromptBuilder {
            return promptBuilder
        }

        fun llm(llm: BaseLanguageModel): Builder {
            this.llm = llm
            return this
        }

        fun tools(tools: List<BaseTool>): Builder {
            this.tools = tools
            return this
        }

        fun callbackManager(callbackManager: BaseCallbackManager): Builder {
            this.callbackManager = callbackManager
            return this
        }

        fun args(args: Map<String, Any>): Builder {
            this.args = args
            return this
        }

        protected open fun applyParams() {
            Companion.applyParams(args, promptBuilder)
            val llmNonNull = llm!!
            if (callbackManager != null && llmNonNull.callbackManager == null) {
                llmNonNull.callbackManager = callbackManager
            }
        }

        override fun build(): ZeroShotAgent {
            applyParams()
            val prompt = promptBuilder.tools(tools!!).build()
            val llmChain = LLMChain(prompt, llm!!, callbackManager = callbackManager)
            val toolNames = tools?.map { it.name } ?: emptyList()
            return ZeroShotAgent(llmChain, toolNames)
        }
    }
}