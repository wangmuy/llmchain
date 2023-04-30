package com.wangmuy.llmchain.llm.openai

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.schema.Generation
import com.wangmuy.llmchain.schema.LLMResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import java.time.Duration

/**
 * retNum: (aka n) How many completions to generate for each prompt.
 */
class OpenAIChat @JvmOverloads constructor(
    private val apiKey: String,
    val invocationParams: MutableMap<String, Any> = mutableMapOf(
        REQ_USER_NAME to "test",
        REQ_MODEL_NAME to "gpt-3.5-turbo",
        REQ_MAX_TOKENS to 50,
        REQ_N to 1,
    ),
    private val proxy: Proxy? = null
): BaseLLM() {
    companion object {
        const val REQ_MODEL_NAME = "model_name"
        const val REQ_USER_NAME = "user_name"
        const val REQ_TEMPERATURE = "temperature"
        const val REQ_MAX_TOKENS = "max_tokens"
        const val REQ_TOP_P = "top_p"
        const val REQ_FREQUENCY_PENALTY = "frequency_penalty"
        const val REQ_PRESENCE_PENALTY = "presence_penalty"
        const val REQ_N = "n"
//        const val REQ_BEST_OF = "best_of"
        const val REQ_LOGIT_BIAS = "logit_bias"
        const val RSP_TOKEN_USAGE = "token_usage"

        private const val TIMEOUT_MILLIS: Long = 10000
    }

    @Volatile
    private var initDone = false

    private lateinit var client: OkHttpClient
    private lateinit var service: OpenAiService
    private val prefixMessages: List<ChatMessage> = mutableListOf()

    private fun initIfNeed() {
        if (initDone) {
            return
        }
        synchronized(this) {
            val mapper = OpenAiService.defaultObjectMapper()
            val builder = OpenAiService.defaultClient(apiKey, Duration.ofMillis(TIMEOUT_MILLIS))
                .newBuilder()
                .addInterceptor(HttpLoggingInterceptor())
            if (proxy != null) {
                builder.proxy(proxy)
            }
            client = builder.build()
            val retrofit = OpenAiService.defaultRetrofit(client, mapper)
            val api: OpenAiApi = retrofit.create(OpenAiApi::class.java)
            service = OpenAiService(api, client.dispatcher().executorService())
            initDone = true
        }
    }

    private fun getChatParams(prompts: List<String>, stop: List<String>?): List<ChatMessage> {
        return mutableListOf<ChatMessage>().also {
            it.addAll(prefixMessages)
            it.add(ChatMessage(ChatMessageRole.USER.value(), prompts[0]))
        }
    }

    override fun onGenerate(prompts: List<String>, stop: List<String>?): LLMResult {
        println("prompt=\n${prompts[0]}")
        initIfNeed()
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
        val response = service.createChatCompletion(builder.build())
        return LLMResult(
            listOf(listOf(Generation(response.choices[0].message.content))),
            mapOf(
                RSP_TOKEN_USAGE to response.usage,
                REQ_MODEL_NAME to modelName))
    }
}