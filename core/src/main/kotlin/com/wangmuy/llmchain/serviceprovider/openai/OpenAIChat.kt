package com.wangmuy.llmchain.serviceprovider.openai

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.schema.Generation
import com.wangmuy.llmchain.schema.LLMResult
import java.net.Proxy

/**
 * retNum: (aka n) How many completions to generate for each prompt.
 */
class OpenAIChat @JvmOverloads constructor(
    apiKey: String,
    val invocationParams: MutableMap<String, Any> = DEFAULT_PARAMS,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false,
    proxy: Proxy? = null,
    service: Any? = null
): BaseLLM(verbose, callbackManager) {
    companion object {
        private val DEFAULT_PARAMS = mutableMapOf<String, Any>(
            REQ_USER_NAME to "test",
            REQ_MODEL_NAME to "gpt-3.5-turbo",
            REQ_MAX_TOKENS to 50,
            REQ_N to 1
        )
    }

    init {
        DEFAULT_PARAMS.filterNot { it.key in invocationParams }.forEach {
            invocationParams[it.key] = it.value
        }
    }

    private val openAiService: OpenAiService

    init {
        if (service == null) {
            ServiceHolder.apiKey = apiKey
            ServiceHolder.proxy = proxy
            openAiService = ServiceHolder.openAiService
        } else {
            openAiService = service as OpenAiService
        }
    }

    private val prefixMessages: List<ChatMessage> = mutableListOf()

    private fun getChatParams(prompts: List<String>, stop: List<String>?): List<ChatMessage> {
        return mutableListOf<ChatMessage>().also {
            it.addAll(prefixMessages)
            it.add(ChatMessage(ChatMessageRole.USER.value(), prompts[0]))
        }
    }

    override fun onGenerate(prompts: List<String>, stop: List<String>?): LLMResult {
        //println("prompt=\n${prompts[0]}")
        val messages = getChatParams(prompts, stop)
        val builder = ChatCompletionRequest.builder()
        if (stop != null) {
            builder.stop(stop)
        }
        val maxTokens = invocationParams[REQ_MAX_TOKENS] as Int?
        if (maxTokens != null && maxTokens > 0) {
            builder.maxTokens(maxTokens)
        }
        val userName = invocationParams[REQ_USER_NAME] as String?
        if (userName != null) {
            builder.user(userName)
        }
        val modelName = invocationParams[REQ_MODEL_NAME] as String
        val retNum = invocationParams[REQ_N] as Int? ?: 1
        builder.model(modelName).messages(messages).n(retNum)
        val temperature = invocationParams[REQ_TEMPERATURE] as Double?
        if (temperature != null) {
            builder.temperature(temperature)
        }
        val topP = invocationParams[REQ_TOP_P] as Double?
        if (topP != null) {
            builder.topP(topP)
        }
        val frequencyPenalty = invocationParams[REQ_FREQUENCY_PENALTY] as Double?
        if (frequencyPenalty != null) {
            builder.frequencyPenalty(frequencyPenalty)
        }
        val presencePenalty = invocationParams[REQ_PRESENCE_PENALTY] as Double?
        if (presencePenalty != null) {
            builder.presencePenalty(presencePenalty)
        }
        val biases = invocationParams[REQ_LOGIT_BIAS] as Map<String, Int>?
        if (biases != null) {
            builder.logitBias(biases)
        }
        // TODO retry

        val response = openAiService.createChatCompletion(builder.build())
        //println("rsp=\n${response.choices[0].message.content}")
        return LLMResult(
            listOf(listOf(Generation(response.choices[0].message.content))),
            mapOf(
                RSP_TOKEN_USAGE to response.usage,
                REQ_MODEL_NAME to modelName))
    }
}