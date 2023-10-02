package com.wangmuy.llmchain.tool

import com.wangmuy.llmchain.callback.BaseCallbackManager
import kotlin.jvm.JvmOverloads

abstract class BaseTool @JvmOverloads constructor(
    val name: String,
    val description: String,
    val returnDirect: Boolean = false,
    val verbose: Boolean = false,
    var callbackManager: BaseCallbackManager? = null
): BaseToolInvoker {
    protected abstract fun onRun(toolInput: String, args: Map<String, Any>?): String // _run

    override fun invoke(toolInput: String, args: Map<String, Any>?): String {
        return run(toolInput, verbose, args)
    }

    fun run(toolInput: String, verbose: Boolean = false, args: Map<String, Any>? = null): String {
        callbackManager?.onToolStart(mapOf(
            "name" to this::class.simpleName as Any, "description" to description), toolInput, verbose, args)
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
    open fun parameterSchema(): Any {
        return Any()
    }
}