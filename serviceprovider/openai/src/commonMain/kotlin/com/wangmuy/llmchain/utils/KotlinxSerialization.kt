package com.wangmuy.llmchain.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

fun JsonElement.toStringWithoutQuotes(): String {
    return this.toString().trim { it.isWhitespace() || it == '\"' }
}

// https://github.com/Kotlin/kotlinx.serialization/issues/746#issuecomment-863099397
fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> JsonArray(map { it.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElement() }.toMap())
    else -> throw IllegalStateException("not supported for non-primitive object")
}

fun Any?.toJsonString(): String = Json.encodeToString(this.toJsonElement())
