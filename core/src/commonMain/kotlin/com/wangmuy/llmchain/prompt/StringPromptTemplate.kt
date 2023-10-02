package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.PromptValue
import kotlin.jvm.JvmOverloads

abstract class StringPromptTemplate @JvmOverloads constructor(
    inputVariables: List<String>, outputParser: BaseOutputParser<Any>? = null)
    : BasePromptTemplate(inputVariables, outputParser) {
    override fun formatPrompt(args: Map<String, Any>?): PromptValue {
        return StringPromptValue(format(args))
    }
}