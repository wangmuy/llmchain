package com.wangmuy.llmchain.chain

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.memory.ConversationBufferMemory
import com.wangmuy.llmchain.prompt.BasePromptTemplate
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.schema.BaseMemory
import java.util.*

open class ConversationChain @JvmOverloads constructor(
    llm: BaseLanguageModel,
    prompt: BasePromptTemplate = PROMPT,
    memory: BaseMemory = ConversationBufferMemory(),
    val inputKey: String = INPUT_KEY,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): LLMChain(prompt, llm, memory = memory, outputKey = OUTPUT_KEY,
    callbackManager = callbackManager, verbose = verbose) {
    override fun inputKeys(): List<String>? {
        return Collections.singletonList(inputKey)
    }

    companion object {
        private const val INPUT_KEY = "input"
        private const val OUTPUT_KEY = "response"

        private const val DEFAULT_TEMPLATE = """The following is a friendly conversation between a human and an AI. The AI is talkative and provides lots of specific details from its context. If the AI does not know the answer to a question, it truthfully says it does not know.

Current conversation:
{history}
Human: {input}
AI:"""
        private val PROMPT = PromptTemplate(listOf("history", "input"), DEFAULT_TEMPLATE)
    }
}