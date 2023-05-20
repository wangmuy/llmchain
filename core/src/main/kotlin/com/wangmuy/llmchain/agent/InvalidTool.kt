package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.tool.BaseTool

class InvalidTool @JvmOverloads constructor(
    name: String = "invalid_tool",
    description: String = "Called when tool name is invalid."
): BaseTool(name, description) {
    override fun onRun(toolName: String, args: Map<String, Any>?): String {
        return "$toolName is not a valid tool, try another one."
    }
}