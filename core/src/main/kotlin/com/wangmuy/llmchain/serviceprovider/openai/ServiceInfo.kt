package com.wangmuy.llmchain.serviceprovider.openai

import com.fasterxml.jackson.databind.ObjectMapper
import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.service.OpenAiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.net.Proxy
import java.time.Duration

class ServiceInfo {
    companion object {
        const val TIMEOUT_MILLIS: Long = 10000
    }

    var baseUrl: String? = null
    var apiKey: String? = null
    var timeoutMillis: Long = TIMEOUT_MILLIS
    var proxy: Proxy? = null

    val client: OkHttpClient by lazy { createClient(apiKey!!, timeoutMillis, proxy) }
    val service: OpenAiService by lazy { createOpenAiService(client, baseUrl!!) }

    fun createClient(apiKey: String, timeout: Long = TIMEOUT_MILLIS, proxy: Proxy? = null): OkHttpClient {
        val builder = OpenAiService.defaultClient(apiKey, Duration.ofMillis(timeout))
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor())
        if (proxy != null) {
            builder.proxy(proxy)
        }
        return builder.build()
    }

    fun createRetrofit(
        client: OkHttpClient, mapper: ObjectMapper,
        baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun createOpenAiService(client: OkHttpClient, baseUrl: String): OpenAiService {
        val mapper = OpenAiService.defaultObjectMapper()
        val retrofit = createRetrofit(client, mapper, baseUrl)
        val api: OpenAiApi = retrofit.create(OpenAiApi::class.java)
        return OpenAiService(api, client.dispatcher().executorService())
    }
}