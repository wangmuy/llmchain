package com.wangmuy.llmchain.memory

import com.wangmuy.llmchain.schema.*
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

object Util {
    @JvmStatic
    fun getPromptInputKey(inputs: Map<String, Any>, memoryVariables: List<String>): String {
        val promptInputKeys = inputs.keys.filter { !memoryVariables.contains(it) && it != "stop" }
        if (promptInputKeys.size != 1) {
            throw IllegalStateException("One input key expected got $promptInputKeys")
        }
        return promptInputKeys[0]
    }

    @JvmStatic
    fun getBufferString(messages: List<BaseMessage>, humanPrefix: String = "Human", aiPrefix: String = "AI"): String {
        val stringMessages = mutableListOf<String>()
        for (m in messages) {
            val role = when (m) {
                is HumanMessage -> humanPrefix
                is AIMessage -> aiPrefix
                is SystemMessage -> "System"
                is ChatMessage -> m.role
                else -> throw IllegalStateException("Got unsupported message type: $m")
            }
            stringMessages.add("$role: ${m.content}")
        }
        return stringMessages.joinToString("\n")
    }
}

open class ChatMessageHistory @JvmOverloads constructor(
    messages: MutableList<BaseMessage> = mutableListOf()
): BaseChatMessageHistory(messages)

abstract class BaseChatMemory @JvmOverloads constructor(
    val chatMemory: BaseChatMessageHistory = ChatMessageHistory(),
    val outputKey: String? = null,
    val inputKey: String? = null,
    val returnMessages: Boolean = false
): BaseMemory() {
    override fun saveContext(inputs: Map<String, Any>, outputs: Map<String, String>) {
        // _get_input_output
        val promptInputKey = inputKey ?: Util.getPromptInputKey(inputs, memoryVariables())
        val outputKey = if (this.outputKey == null) {
            if (outputs.size != 1) {
                throw IllegalArgumentException("One output key expected, got ${outputs.keys}")
            }
            outputs.keys.iterator().next()
        } else {
            this.outputKey
        }
        // save_context
        chatMemory.addUserMessage(inputs[promptInputKey] as String)
        chatMemory.addAIMessage(outputs[outputKey]!!)
    }

    override fun clear() {
        chatMemory.clear()
    }
}

open class ConversationBufferMemory @JvmOverloads constructor(
    val humanPrefix: String = "Human",
    val aiPrefix: String = "AI",
    val memoryKey: String = "history"
): BaseChatMemory() {
    fun getBuffer(): List<BaseMessage> {
        return if (returnMessages)
            chatMemory.messages
        else
            listOf(BaseMessage(Util.getBufferString(
                chatMemory.messages, humanPrefix, aiPrefix)))
    }

    override fun memoryVariables(): List<String> {
        return listOf(memoryKey)
    }

    override fun loadMemoryVariables(inputs: Map<String, Any>): Map<String, Any> {
        return mapOf(memoryKey to getBuffer())
    }
}