package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.PromptValue

abstract class StringPromptTemplate @JvmOverloads constructor(inputVariables: List<String>)
    : BasePromptTemplate(inputVariables) {
    override fun formatPrompt(args: Map<String, Any>?): PromptValue {
        return StringPromptValue(format(args))
    }
}