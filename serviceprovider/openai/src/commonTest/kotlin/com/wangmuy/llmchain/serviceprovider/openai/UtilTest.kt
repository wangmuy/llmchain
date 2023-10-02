package com.wangmuy.llmchain.serviceprovider.openai

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test

class UtilTest {
    @Test fun jsonTest() {
        val json = buildJsonObject {
            put("name", "get_weather_info")
            put("arguments", buildJsonObject {
                put("location", "Boston, CA")
            })
        }
        println("json=$json")
        val name = json["name"]?.jsonPrimitive?.content
        println("name=${name}")
    }
}