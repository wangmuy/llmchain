package com.wangmuy.llmchain.serviceprovider.openai

import com.aallam.openai.api.chat.ChatCompletionFunction
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionMode
import com.aallam.openai.api.chat.Parameters
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.schema.Generation
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * retNum: (aka n) How many completions to generate for each prompt.
 */
class OpenAIChat @JvmOverloads constructor(
    apiKey: String,
    val invocationParams: MutableMap<String, Any> = DEFAULT_PARAMS,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false,
    baseUrl: String = OPENAI_BASE_URL,
    timeoutMillis: Long = ServiceInfo.TIMEOUT_MILLIS,
    proxy: String? = null,
): BaseLLM(verbose, callbackManager) {
    companion object {
        const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
        private val DEFAULT_PARAMS = mutableMapOf<String, Any>(
            REQ_USER_NAME to "test",
            REQ_MODEL_NAME to "gpt-3.5-turbo",
            REQ_MAX_TOKENS to 50,
            REQ_N to 1
        )
    }

    private val openAiService: OpenAI

    init {
        DEFAULT_PARAMS.filterNot { it.key in invocationParams }.forEach {
            invocationParams[it.key] = it.value
        }

        ServiceHolder.serviceInfo.baseUrl = baseUrl
        ServiceHolder.serviceInfo.apiKey = apiKey
        ServiceHolder.serviceInfo.timeoutMillis = timeoutMillis
        ServiceHolder.serviceInfo.proxy = proxy
        openAiService = ServiceHolder.serviceInfo.service
    }

    private val prefixMessages: List<ChatMessage> = mutableListOf()

    private fun getChatParams(prompts: List<String>, stop: List<String>?): List<ChatMessage> {
        return mutableListOf<ChatMessage>().also {
            it.addAll(prefixMessages)
            it.add(ChatMessage(ChatRole.User, prompts[0]))
        }
    }

    override fun onGenerate(prompts: List<String>, stop: List<String>?, inputList: List<Map<String, Any>>): LLMResult {
        //println("prompt=\n${prompts[0]}")
        val messages = getChatParams(prompts, stop)
        val modelName = invocationParams[REQ_MODEL_NAME] as String

        val funcInfoMap = inputList.firstOrNull {it.containsKey(FunctionUtil.KEY_FUNCTION_CALL) }
        val funcTools = funcInfoMap?.get(FunctionUtil.KEY_FUNCTION_CALL) as List<BaseTool>?
        val functions = funcTools?.map {tool->
            ChatCompletionFunction(tool.name, tool.description, Parameters(tool.parameterSchema()))
        }
        val funcMode = (funcInfoMap?.get(FunctionUtil.KEY_FUNCTION_MODE) as String?).let {
            when (it) {
                "auto" -> FunctionMode.Auto
                "none", "" -> FunctionMode.None
                else -> null
            }
        }

        val request = ChatCompletionRequest(
            messages = messages,
            stop = stop,
            maxTokens = invocationParams[REQ_MAX_TOKENS] as Int?,
            user = invocationParams[REQ_USER_NAME] as String?,
            model = ModelId(modelName),
            n = invocationParams[REQ_N] as Int?,
            temperature = invocationParams[REQ_TEMPERATURE] as Double?,
            topP = invocationParams[REQ_TOP_P] as Double?,
            frequencyPenalty = invocationParams[REQ_FREQUENCY_PENALTY] as Double?,
            presencePenalty = invocationParams[REQ_PRESENCE_PENALTY] as Double?,
            logitBias = invocationParams[REQ_LOGIT_BIAS] as Map<String, Int>?,
            functions = functions,
            functionCall = funcMode
        )

        val response = runBlocking { openAiService.chatCompletion(request) }
        val msg = response.choices[0].message
        val content = msg.content ?: ""
        val outputMap = mutableMapOf(
            RSP_TOKEN_USAGE to response.usage as Any,
            REQ_MODEL_NAME to modelName,
            RSP_MESSAGE to msg
        )
        val functionCall = msg.functionCall
        val funcJson = if (functionCall != null) {
            buildJsonObject {
                put("name", functionCall.name)
                put("arguments", functionCall.argumentsAsJson())
            }
        } else null
        if (funcJson != null) {
            outputMap[FunctionUtil.KEY_FUNCTION_CALL] = funcJson
        }
        //println("rsp=\n${response.choices[0].message.content}")
        return LLMResult(
            listOf(listOf(Generation(content))),
            outputMap
        )
    }
}