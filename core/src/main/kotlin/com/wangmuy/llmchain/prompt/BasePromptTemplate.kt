package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.PromptValue
import java.util.Collections

abstract class BasePromptTemplate @JvmOverloads constructor(
    val inputVariables: List<String> = Collections.emptyList(),
    val outputParser: BaseOutputParser<Any>? = null
) {
    abstract fun formatPrompt(args: Map<String, Any>?): PromptValue
    abstract fun format(args: Map<String, Any>?): String
}