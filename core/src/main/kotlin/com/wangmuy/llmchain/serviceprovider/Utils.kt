package com.wangmuy.llmchain.serviceprovider

import java.net.InetSocketAddress
import java.net.Proxy

object Utils {
    fun parseProxy(proxy: String?): Triple<String?, String?, Int> {
        val splits = proxy?.split("://")
        val protocol = splits?.get(0)
        val hostPortSplits = splits?.get(1)?.split(":")
        val host = hostPortSplits?.get(0)
        val port = (hostPortSplits?.getOrNull(1)?.ifEmpty { "80" } ?: "80").toInt()
        return Triple(protocol, host, port)
    }

    fun getProxy(proxyStr: String?): Proxy? {
        val (protocol, host, port) = parseProxy(proxyStr)
        return when (protocol) {
            "http" -> Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
            "socks5", "socks" -> Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port))
            else -> null
        }
    }
}