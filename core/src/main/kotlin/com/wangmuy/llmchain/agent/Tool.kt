package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.tool.BaseTool

open class Tool @JvmOverloads constructor(
    name: String,
    private val func: (String, Map<String, Any>?) -> String,
    description: String = "",
    returnDirect: Boolean = false,
    private val args: Map<String, Any>? = null
): BaseTool(name, description, returnDirect) {
    override fun onRun(toolInput: String, args: Map<String, Any>?): String {
        return func.invoke(toolInput, args)
    }
}