package com.wangmuy.llmchain.serviceprovider.openai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import com.wangmuy.llmchain.serviceprovider.Utils
import kotlin.time.Duration.Companion.milliseconds

class ServiceInfo {
    companion object {
        const val TIMEOUT_MILLIS: Long = 10000
    }

    var baseUrl: String? = null
    var apiKey: String? = null
    var timeoutMillis: Long = TIMEOUT_MILLIS
    var proxy: String? = null

    val service: OpenAI by lazy { createOpenAiService(baseUrl!!, apiKey!!, timeoutMillis) }

    fun createOpenAiService(
        baseUrl: String, apiKey: String, timeoutMillis: Long): OpenAI {
        val (proxyProtocol, host, port) = Utils.parseProxy(proxy)
        return OpenAI(
            token = apiKey,
            timeout = Timeout(socket = timeoutMillis.milliseconds),
            host = OpenAIHost(baseUrl = baseUrl),
            proxy = when (proxyProtocol) {
                "http" -> ProxyConfig.Http(proxy!!)
                "socks5",
                "socks" -> ProxyConfig.Socks(host!!, port)
                else -> null
            }
        )
    }
}