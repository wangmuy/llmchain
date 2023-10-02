package com.wangmuy.llmchain.outputparser

import com.wangmuy.llmchain.schema.BaseOutputParser

/**
 * <JsonElement>
 */
open class JsonOutputParser: BaseOutputParser<Any> {
    companion object {
        val PTNSTR_JSON_QUOTED = "```(json)?(.*)```"
        // todo: RegexOption.DOT_MATCHES_ALL only available in jvm/native
        var PATTERN_JSON_QUOTED = Regex(PTNSTR_JSON_QUOTED)

        /**
         * @return JsonElement
         */
        fun parseJsonMarkdown(str: String): Any {
            val result = PATTERN_JSON_QUOTED.find(str)
            val jsonStr = if (result == null) {
                str
            } else {
                result.groupValues[2]
            }
            return parser!!.parseToJson(jsonStr)
        }

        /**
         * @return JsonElement
         */
        fun parseAndCheckJsonMarkdown(text: String, expectedKeys: List<String>): Any {
            val json = parseJsonMarkdown(text)
            val keys = parser!!.getKeys(json)
            for (key in expectedKeys) {
                if (key !in keys) {
                    throw IllegalStateException("Got invalid return object. Expected key $key to be present, but got $json")
                }
            }
            return json
        }

        fun toStringWithoutQuotes(json: Any): String {
            return json.toString().trim { it.isWhitespace() || it == '\"' }
        }

        var parser: JsonParser? = null
    }

    interface JsonParser {
        fun parseToJson(str: String): Any
        fun get(json: Any, key: String): Any?
        fun getKeys(json: Any): Set<String>
    }

    /**
     * @return JsonElement
     */
    override fun parse(text: String): Any {
        return parseJsonMarkdown(text)
    }
}