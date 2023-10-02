package com.wangmuy.llmchain.serviceprovider.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.wangmuy.llmchain.serviceprovider.Utils
import com.wangmuy.llmchain.utils.coroutine.runBlockingKMP
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(BetaOpenAI::class)
class OpenAIEndpointTest {
    companion object {
        private val TOKEN_KEY = openAIKey
        private val BASEURL = openAIBaseUrl
        private val TIMEOUT_MILLIS: Long = openAITimeoutMillis
        private val PROXY = openAIProxy
    }

    private lateinit var service: OpenAI

    @BeforeTest fun before() {
        val (proxyProtocol, host, port) = Utils.parseProxy(PROXY)
        service = OpenAI(
            token = TOKEN_KEY,
            timeout = Timeout(socket = TIMEOUT_MILLIS.milliseconds),
            host = OpenAIHost(baseUrl = BASEURL),
            proxy = when (proxyProtocol) {
                "http" -> ProxyConfig.Http(PROXY!!)
                "socks5",
                "socks" -> ProxyConfig.Socks(host!!, port)
                else -> null
            }
        )
    }

    @Test fun testChat() {
        val systemMessage = ChatMessage(ChatRole.User, "Tell me a joke.")
        val messages = listOf(systemMessage)
        val biases = HashMap<String, Int>()
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messages,
            n = 1,
            maxTokens = 50,
            logitBias = biases
        )
        val response = runBlockingKMP { service.chatCompletion(chatCompletionRequest) }
        response.choices.forEach(::println)
    }

    @Test fun testFunctionCall() {
        val messages = listOf(
            ChatMessage(ChatRole.System, "You are an helpful assistant."),
            ChatMessage(ChatRole.User, "What's the weather like in SF?")
        )
        val weatherFunc = ChatCompletionFunction(
            name = "currentWeather",
            description = "Get the current weather in a given location",
            parameters = Parameters.buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("location") {
                        put("type", "string")
                        put("description", "The city and state, e.g. San Francisco, CA")
                    }
                    putJsonObject("unit") {
                        put("type", "string")
                        putJsonArray("enum") {
                            add("celsius")
                            add("fahrenheit")
                        }
                    }
                }
                putJsonArray("required") {
                    add("location")
                    add("unit")
                }
            }
        )
        val request = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messages,
            n = 1,
            maxTokens = 100,
            functions = listOf(weatherFunc),
            functionCall = FunctionMode.Auto
        )
        val responseMessage = runBlockingKMP { service.chatCompletion(request).choices[0].message }
        val functionCall = responseMessage?.functionCall
        println("functionCall name=${functionCall?.name}, arguments=${functionCall?.arguments}")
    }
}