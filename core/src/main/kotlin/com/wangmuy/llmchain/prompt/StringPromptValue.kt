package com.wangmuy.llmchain.prompt

import com.wangmuy.llmchain.schema.BaseMessage
import com.wangmuy.llmchain.schema.HumanMessage
import com.wangmuy.llmchain.schema.PromptValue

class StringPromptValue(
        val text: String
): PromptValue() {
    override fun asString(): String {
        return text
    }

    override fun asMessage(): List<BaseMessage> {
        return listOf(HumanMessage(text))
    }
}