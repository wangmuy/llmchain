package com.wangmuy.llmchain.llm

object FunctionUtil {
    const val KEY_FUNCTION_CALL = "_functionCall"
    const val KEY_FUNCTION_MODE = "_functionMode"
    const val FUNCTION_CALL_PREFIX = "FunctionCall:"

    interface JsonParser {
        fun parseToJson(str: String): Any
        fun get(json: Any, key: String): Any?
    }

    var parser: JsonParser? = null
}