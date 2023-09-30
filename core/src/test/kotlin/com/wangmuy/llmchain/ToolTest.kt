package com.wangmuy.llmchain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.agent.Tool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.Test

class ToolTest {
    @Test fun formatToolTest() {
        val tool = object : Tool(name = "testTool", description = "helpful tool", func = {x, _-> x}) {
            override fun parameterSchema(): JsonObject {
                return buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("location") {
                            put("type", "string")
                            put("description", "The city and state, e.g. San Francisco, CA")
                        }
                        putJsonObject("unit") {
                            put("type", "string")
                            put("description", "The weather temperature unit")
                            putJsonArray("enum") {
                                add("celsius")
                                add("fahrenheit")
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("location")
                        add("unit")
                    }
                }
            }
        }
        val schemaStr = FunctionUtil.formatToolToOpenAIFunctionString(tool)
        println("schema=$schemaStr")
    }

    enum class WeatherUnit {
        CELSIUS, FAHRENHEIT
    }

    class WeatherPOJO(
        @get: JsonPropertyDescription("The city and state, e.g. San Francisco, CA")
        val location: String,

        @get: JsonPropertyDescription("The weather temperature unit")
        @get: JsonProperty(required = true)
        val unit: WeatherUnit
    )

    @Test fun formatToolJsonSchemaTest() {
        val mapper = ObjectMapper().registerKotlinModule()
        val schemaGen = JsonSchemaGenerator(mapper)
        val tool = object : Tool(name = "testTool", description = "helpful tool", func = {x, _-> x}) {
            override fun parameterSchema(): JsonObject {
                val schema = schemaGen.generateSchema(WeatherPOJO::class.java)
                schema.id = null
                return Json.decodeFromString(mapper.writer().writeValueAsString(schema))
            }
        }
        val schemaStr = FunctionUtil.formatToolToOpenAIFunctionString(tool)
        println("schema=$schemaStr")
    }
}