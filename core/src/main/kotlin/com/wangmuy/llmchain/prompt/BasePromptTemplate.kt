package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.PromptValue
import java.util.*

abstract class BasePromptTemplate @JvmOverloads constructor(
    val inputVariables: List<String> = Collections.emptyList(),
    val outputParser: BaseOutputParser<String>? = null
) {
    abstract fun formatPrompt(args: Map<String, Any>?): PromptValue
    abstract fun format(args: Map<String, Any>?): String
}