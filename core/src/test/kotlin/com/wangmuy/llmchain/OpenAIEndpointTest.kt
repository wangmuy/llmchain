package com.wangmuy.llmchain

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.completion.CompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import java.time.Duration

class OpenAIEndpointTest {
    companion object {
        const val TOKEN_KEY = "sk-"
        const val USERNAME = "test"
    }

    private lateinit var client: OkHttpClient
    private lateinit var service: OpenAiService

    @Before fun before() {
        val mapper = OpenAiService.defaultObjectMapper()
        val builder = OpenAiService.defaultClient(TOKEN_KEY, Duration.ofMillis(10000))
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor())
//        val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("172.25.253.30", 80))
//        builder.proxy(proxy)
        client = builder.build()
        val retrofit = OpenAiService.defaultRetrofit(client, mapper)
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
}