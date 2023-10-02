package com.wangmuy.llmchain.serviceprovider

object Utils {
    fun parseProxy(proxy: String?): Triple<String?, String?, Int> {
        val splits = proxy?.split("://")
        val protocol = splits?.get(0)
        val hostPortSplits = splits?.get(1)?.split(":")
        val host = hostPortSplits?.get(0)
        val port = (hostPortSplits?.getOrNull(1)?.ifEmpty { "80" } ?: "80").toInt()
        return Triple(protocol, host, port)
    }
}