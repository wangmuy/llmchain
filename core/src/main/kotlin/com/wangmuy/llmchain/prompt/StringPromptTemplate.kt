package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.PromptValue

abstract class StringPromptTemplate @JvmOverloads constructor(
    inputVariables: List<String>, outputParser: BaseOutputParser<String>? = null)
    : BasePromptTemplate(inputVariables, outputParser) {
    override fun formatPrompt(args: Map<String, Any>?): PromptValue {
        return StringPromptValue(format(args))
    }
}