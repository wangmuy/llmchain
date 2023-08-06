package com.wangmuy.llmchain.tool

interface BaseToolInvoker {
    fun invoke(toolInput: String, args: Map<String, Any>?): String
}