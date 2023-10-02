package com.wangmuy.llmchain.outputparser

import com.wangmuy.llmchain.schema.BaseOutputParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

open class JsonOutputParser: BaseOutputParser<JsonElement> {
    companion object {
        val PATTERN_JSON_QUOTED = Regex("```(json)?(.*)```", RegexOption.DOT_MATCHES_ALL)

        fun parseJsonMarkdown(str: String): JsonElement {
            val result = PATTERN_JSON_QUOTED.find(str)
            val jsonStr = if (result == null) {
                str
            } else {
                result.groupValues[2]
            }
            return Json.parseToJsonElement(jsonStr)
        }

        fun parseAndCheckJsonMarkdown(text: String, expectedKeys: List<String>): JsonElement {
            val json = parseJsonMarkdown(text)
            val keys = json.jsonObject
            for (key in expectedKeys) {
                if (key !in keys) {
                    throw IllegalStateException("Got invalid return object. Expected key $key to be present, but got $json")
                }
            }
            return json
        }
    }

    override fun parse(text: String): JsonElement {
        return parseJsonMarkdown(text)
    }
}