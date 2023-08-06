package com.wangmuy.llmchain.llm

import com.wangmuy.llmchain.schema.BaseMessage

interface BaseChatInvoker {
    fun invoke(messages: List<BaseMessage>, stop: List<String>?): BaseMessage
}