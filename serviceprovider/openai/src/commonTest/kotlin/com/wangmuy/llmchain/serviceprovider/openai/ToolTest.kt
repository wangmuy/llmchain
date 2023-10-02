package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.llm.formatToolToOpenAIFunctionString
import kotlinx.serialization.json.*
import kotlin.test.Test

class ToolTest {
    @Test fun formatToolTest() {
        val tool = object : Tool(name = "testTool", description = "helpful tool", func = { x, _-> x}) {
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
}