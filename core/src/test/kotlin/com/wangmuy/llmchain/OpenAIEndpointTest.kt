package com.wangmuy.llmchain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.theokanning.openai.client.OpenAiApi
import com.theokanning.openai.completion.CompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatFunction
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration
import kotlin.random.Random

class OpenAIEndpointTest {
    companion object {
        private val BASEURL = OpenAIChatTest.BASEURL
        private val TOKEN_KEY = OpenAIChatTest.APIKEY
        private val PROXY = OpenAIChatTest.PROXY
        private const val USERNAME = "test"
    }

    private lateinit var client: OkHttpClient
    private lateinit var service: OpenAiService

    @Before fun before() {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val mapper = OpenAiService.defaultObjectMapper().registerKotlinModule()
        val builder = OpenAiService.defaultClient(TOKEN_KEY, Duration.ofMillis(10000))
            .newBuilder()
            .addInterceptor(logging)
            .proxy(PROXY)
        client = builder.build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASEURL)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val api: OpenAiApi = retrofit.create(OpenAiApi::class.java)
        service = OpenAiService(api, client.dispatcher().executorService())
    }

    @Test fun testCompletion() {
        val completionRequest = CompletionRequest.builder()
            .model("ada").prompt("Somebody once told me the world is gonna roll me").echo(true).user(USERNAME).n(3)
            .build()
        val response = service.createCompletion(completionRequest)
        response.choices.forEach(::println)
    }

    @Test fun testChat() {
        val systemMessage = ChatMessage(ChatMessageRole.SYSTEM.value(), "Tell me a joke.")
        val messages = listOf(systemMessage)
        val biases = HashMap<String, Int>()
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo").messages(messages).n(1).maxTokens(50).logitBias(biases)
            .build()
        val response = service.createChatCompletion(chatCompletionRequest)
        response.choices.forEach(::println)
    }

    @Test fun testChatStream() {
        val systemMessage = ChatMessage(ChatMessageRole.SYSTEM.value(), "Tell me a joke.")
        val messages = listOf(systemMessage)
        val biases = HashMap<String, Int>()
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo").messages(messages).n(1)
            .maxTokens(50).logitBias(biases).user(USERNAME)
            .build()
        service.streamChatCompletion(chatCompletionRequest)
            .doOnError(Throwable::printStackTrace)
            .blockingForEach(::println)
        service.shutdownExecutor()
    }

    enum class WeatherUnit {
        CELSIUS, FAHRENHEIT
    }

    // https://stackoverflow.com/a/47984845
    class Weather(
        @get: JsonPropertyDescription("City and state, for example: Leon, Guanajuato")
        val location: String,

        @get: JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @get: JsonProperty(required = true)
        val unit: WeatherUnit
    )

    class WeatherResponse(
        val location: String, val unit: WeatherUnit, val temperature: Int, val description: String
    )

    @Test fun testFunctionCall() {
        val messages = listOf(
            ChatMessage(ChatMessageRole.SYSTEM.value(), "You are an helpful assistant."),
            ChatMessage(ChatMessageRole.USER.value(), "What's the weather like in SF?")
        )
        val weatherFunc = ChatFunction.builder()
            .name("get_weather")
            .description("Get the current weather of a location")
//            .executor(Weather::class.java) { w ->
//                WeatherResponse(
//                    w.location,
//                    w.unit,
//                    Random.Default.nextInt(50),
//                    "sunny"
//                )
//            }
            .executor(Weather::class.java, null)
            .build()
        val request = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .functions(listOf(weatherFunc))
            .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
            .n(1)
            .maxTokens(100)
//            .logitBias(HashMap())
            .build()
        val responseMessage = service.createChatCompletion(request).choices[0].message
        val functionCall = responseMessage.functionCall
        println("functionCall name=${functionCall.name}, ${functionCall.arguments}")
    }
}