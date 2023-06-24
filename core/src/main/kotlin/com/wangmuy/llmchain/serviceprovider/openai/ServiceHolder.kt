package com.wangmuy.llmchain.serviceprovider.openai

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.service.OpenAiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import java.time.Duration

internal object ServiceHolder {
    const val TIMEOUT_MILLIS: Long = 10000
    var apiKey: String? = null
    var proxy: Proxy? = null

    val client: OkHttpClient by lazy { createClient(apiKey!!, proxy) }
    val openAiService: OpenAiService by lazy { createOpenAiService(client) }

    fun createClient(apiKey: String, proxy: Proxy?): OkHttpClient {
        val builder = OpenAiService.defaultClient(apiKey, Duration.ofMillis(TIMEOUT_MILLIS))
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor())
        if (proxy != null) {
            builder.proxy(proxy)
        }
        return builder.build()
    }

    fun createOpenAiService(client: OkHttpClient): OpenAiService {
        val mapper = OpenAiService.defaultObjectMapper()
        val retrofit = OpenAiService.defaultRetrofit(client, mapper)
        val api: OpenAiApi = retrofit.create(OpenAiApi::class.java)
        return OpenAiService(api, client.dispatcher().executorService())
    }
}