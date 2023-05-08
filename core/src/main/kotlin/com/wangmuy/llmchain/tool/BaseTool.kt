package com.wangmuy.llmchain.tool

import com.wangmuy.llmchain.callback.BaseCallbackManager

abstract class BaseTool @JvmOverloads constructor(
    val name: String,
    val description: String,
    val returnDirect: Boolean = false,
    val verbose: Boolean = false,
    var callbackManager: BaseCallbackManager? = null
): (String) -> String {
    protected abstract fun onRun(toolInput: String): String // _run

    override fun invoke(toolInput: String): String {
        return onRun(toolInput)
    }

    fun run(toolInput: String, verbose: Boolean = false, args: Map<String, Any>? = null): String {
        callbackManager?.onToolStart(mapOf(
            "name" to javaClass.name, "description" to description), toolInput, verbose)
        try {
            val observation = onRun(toolInput)
            callbackManager?.onToolEnd(observation, verbose)
            return observation
        } catch (e: Exception) {
            callbackManager?.onToolError(e, verbose)
            throw e
        }
    }
}