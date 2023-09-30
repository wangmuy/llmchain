package com.wangmuy.llmchain.tool

import com.wangmuy.llmchain.callback.BaseCallbackManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

abstract class BaseTool @JvmOverloads constructor(
    val name: String,
    val description: String,
    val returnDirect: Boolean = false,
    val verbose: Boolean = false,
    var callbackManager: BaseCallbackManager? = null
): (String, Map<String, Any>?) -> String {
    protected abstract fun onRun(toolInput: String, args: Map<String, Any>?): String // _run

    override fun invoke(toolInput: String, args: Map<String, Any>?): String {
        return run(toolInput, verbose, args)
    }

    fun run(toolInput: String, verbose: Boolean = false, args: Map<String, Any>? = null): String {
        callbackManager?.onToolStart(mapOf(
            "name" to javaClass.name, "description" to description), toolInput, verbose, args)
        try {
            val observation = onRun(toolInput, args)
            callbackManager?.onToolEnd(observation, verbose, args)
            return observation
        } catch (e: Exception) {
            callbackManager?.onToolError(e, verbose, args)
            throw e
        }
    }

    /** Only used in function call */
    open fun parameterSchema(): JsonObject {
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