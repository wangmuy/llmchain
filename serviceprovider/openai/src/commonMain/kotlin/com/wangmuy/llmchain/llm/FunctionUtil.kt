package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.serialization.json.*

fun FunctionUtil.formatToolToOpenAIFunctionString(
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

fun FunctionUtil.formatToolToOpenAIFunctionString(tool: BaseTool): String {
    val schema = formatToolToOpenAIFunctionString(
        tool.name, tool.description, tool.parameterSchema() as JsonObject)
    return schema.toString()
}