package com.wangmuy.llmchain.schema

import com.wangmuy.llmchain.callback.BaseCallbackManager

open class BaseAgentAction(val log: String)

open class AgentAction(
    val tool: String,
    val toolInput: String,
    log: String
): BaseAgentAction(log)

open class AgentFinish(
    val returnValues: Map<String, Any>,
    log: String
): BaseAgentAction(log)

open class Generation @JvmOverloads constructor(
    val text: String,
    val generationInfo: Map<String, Any>? = null
) {
    override fun toString(): String {
        return "Generation(text='$text', generationInfo=$generationInfo)"
    }
}

open class BaseMessage @JvmOverloads constructor(
    val content: String,
    val additionalArgs: Map<String, String>? = null
) {
    open fun msgType(): String {
        return "str"
    }

    override fun toString(): String {
        return content
    }
}

open class HumanMessage @JvmOverloads constructor(
    content: String, additionalArgs: Map<String, String>? = null)
    : BaseMessage(content, additionalArgs) {
    override fun msgType(): String {
        return "human"
    }
}

open class AIMessage @JvmOverloads constructor(
    content: String, additionalArgs: Map<String, String>? = null)
    : BaseMessage(content, additionalArgs) {
    override fun msgType(): String {
        return "ai"
    }
}

open class SystemMessage @JvmOverloads constructor(
    content: String, additionalArgs: Map<String, String>? = null)
    : BaseMessage(content, additionalArgs) {
    override fun msgType(): String {
        return "system"
    }
}

open class FunctionMessage @JvmOverloads constructor(
    name: String,
    content: String, additionalArgs: Map<String, String>? = null)
    : BaseMessage(content, additionalArgs) {
    override fun msgType(): String {
        return "function"
    }
}

open class ChatMessage @JvmOverloads constructor(
    val role: String, content: String, additionalArgs: Map<String, String>? = null)
    : BaseMessage(content, additionalArgs) {
    override fun msgType(): String {
        return "chat"
    }
}

open class ChatGeneration(text: String, val message: BaseMessage): Generation(text)

open class ChatResult(val generations: List<ChatGeneration>, val llmOutput: Map<String, String>?)

open class LLMResult @JvmOverloads constructor(
        val generations: List<List<Generation>>,
        val llmOutput: Map<String, Any>? = null
) {
    override fun toString(): String {
        return "LLMResult(generations=$generations, llmOutput=$llmOutput)"
    }
}

abstract class PromptValue {
    abstract fun asString(): String
    abstract  fun asMessage(): List<BaseMessage>
    override fun toString(): String {
        return asString()
    }
}

abstract class BaseLanguageModel(var callbackManager: BaseCallbackManager? = null) {
    abstract fun generatePrompt(
        prompts: List<PromptValue>,
        stop: List<String>?,
        inputList: List<Map<String, Any>> = emptyList()
    ): LLMResult
}

abstract class BaseMemory {
    abstract fun memoryVariables(): List<String>
    abstract fun loadMemoryVariables(inputs: Map<String, Any>): Map<String, Any>
    abstract fun saveContext(inputs: Map<String, Any>, outputs: Map<String, String>)
    abstract fun clear()
}

abstract class BaseChatMessageHistory @JvmOverloads constructor(
    val messages: MutableList<BaseMessage> = mutableListOf()) {
    open fun addUserMessage(message: String) {
        messages.add(HumanMessage(message))
    }

    open fun addAIMessage(message: String) {
        messages.add(AIMessage(message))
    }

    open fun clear() {
        messages.clear()
    }
}

open class Document(val pageContent: String, val metadata: Map<String, Any>? = null) {
    override fun equals(other: Any?): Boolean {
        return other is Document && other.pageContent == pageContent && other.metadata == metadata
    }

    override fun hashCode(): Int {
        var result = pageContent.hashCode()
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Document(pageContent='$pageContent', metadata=$metadata)"
    }
}

interface BaseRetriever {
    fun getRelevantDocuments(query: String): List<Document>
}

interface BaseOutputParser<T> {
    @Throws(OutputParserException::class)
    fun parse(text: String): T

    @Throws(OutputParserException::class)
    fun parseWithPrompt(completion: String, prompt: PromptValue): T {
        return parse(completion)
    }

    fun getFormatInstructions(): String {
        throw NotImplementedError("Default not implemented")
    }
}

class OutputParserException(message: String = "", cause: Throwable?): Exception(message, cause)