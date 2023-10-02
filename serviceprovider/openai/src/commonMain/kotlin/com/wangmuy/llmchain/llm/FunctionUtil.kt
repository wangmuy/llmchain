package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.callback.BaseCallbackManager
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

class BaseToolWithDefaultParameterSchema(
    val proxy: BaseTool
): BaseTool(proxy.name, proxy.description, proxy.returnDirect, proxy.verbose, proxy.callbackManager) {
    override fun onRun(toolInput: String, args: Map<String, Any>?): String {
        return proxy.onRun(toolInput, args)
    }

    override fun parameterSchema(): Any {
        return buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("__arg1") {
                    put("type", "string")
                    put("description", "first argument")
                }
            }
            putJsonArray("required") {
                add("__arg1")
            }
        }
    }
}

fun BaseTool.withDefaultParameterSchema(): BaseTool {
    return BaseToolWithDefaultParameterSchema(this)
}