package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.tool.BaseTool

class Tool @JvmOverloads constructor(
    name: String,
    private val func: (String) -> String,
    description: String = "",
    returnDirect: Boolean = false,
    private val args: Map<String, Any>? = null
): BaseTool(name, description, returnDirect) {
    override fun onRun(toolInput: String): String {
        return func.invoke(toolInput)
    }
}