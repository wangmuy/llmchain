package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object FunctionUtil {
    const val KEY_FUNCTION_CALL = "_functionCall"
    const val KEY_FUNCTION_MODE = "_functionMode"

    fun formatToolToOpenAIFunctionString(
        name: String,
        description: String,
        parameterSchema: JsonObject = buildJsonObject {},
        parameterBuildBlock: (JsonObjectBuilder.() -> Unit)? = null): JsonElement {
        return buildJsonObject {
            put("name", name)
            put("description", description)
            if (parameterBuildBlock != null) {
                putJsonObject("parameters", parameterBuildBlock)
            } else {
                put("parameters", parameterSchema)
            }
        }
    }

    fun formatToolToOpenAIFunctionString(tool: BaseTool): String {
        val schema = formatToolToOpenAIFunctionString(
            tool.name, tool.description, tool.parameterSchema())
        return schema.toString()
    }
}