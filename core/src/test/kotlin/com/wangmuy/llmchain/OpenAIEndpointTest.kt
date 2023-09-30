package com.wangmuy.llmchain

import com.aallam.openai.api.chat.ChatCompletionFunction
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionMode
import com.aallam.openai.api.chat.Parameters
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.wangmuy.llmchain.serviceprovider.Utils
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class OpenAIEndpointTest {
    companion object {
        private val BASEURL = OpenAIChatTest.BASEURL
        private val TOKEN_KEY = OpenAIChatTest.APIKEY
        private val TIMEOUT_MILLIS = OpenAIChatTest.TIMEOUT_MILLIS
        private val PROXY = OpenAIChatTest.PROXY
        private const val USERNAME = "test"
    }

    private lateinit var service: OpenAI

    @Before fun before() {
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
        val response = runBlocking { service.chatCompletion(chatCompletionRequest) }
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
        val responseMessage = runBlocking { service.chatCompletion(request).choices[0].message }
        val functionCall = responseMessage.functionCall
        println("functionCall name=${functionCall?.name}, arguments=${functionCall?.arguments}")
    }
}