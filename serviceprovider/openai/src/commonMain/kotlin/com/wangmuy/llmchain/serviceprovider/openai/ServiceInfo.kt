package com.wangmuy.llmchain.serviceprovider.openai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import kotlin.time.Duration.Companion.milliseconds

class ServiceInfo {
    companion object {
        const val TIMEOUT_MILLIS: Long = 10000
    }

    var baseUrl: String? = null
    var apiKey: String? = null
    var timeoutMillis: Long = TIMEOUT_MILLIS
    var proxy: String? = null

    val service: OpenAI by lazy { createOpenAIService(baseUrl!!, apiKey!!, timeoutMillis) }

    private fun createOpenAIService(
        baseUrl: String, apiKey: String, timeoutMillis: Long): OpenAI {
        val proxySplits = proxy?.split("://")
        val proxyProtocol = proxySplits?.get(0)
        val hostPort = proxySplits?.get(1)
        val hostPortSplit = hostPort?.split(":")
        val host = hostPortSplit?.get(0)
        val port = if (host != null) (hostPortSplit[1].ifEmpty { "80" }.toInt()) else null
        return OpenAI(
            token = apiKey,
            timeout = Timeout(socket = timeoutMillis.milliseconds),
            host = OpenAIHost(baseUrl = baseUrl),
            proxy = when (proxyProtocol) {
                "http" -> ProxyConfig.Http(proxy!!)
                "socks5",
                "socks" -> ProxyConfig.Socks(host!!, port!!)
                else -> null
            }
        )
    }
}