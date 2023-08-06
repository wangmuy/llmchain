package com.wangmuy.llmchain.chain

interface ChainInvoker {
    fun invoke(inputs: Map<String, Any>?): Map<String, Any>
}