package com.wangmuy.llmchain.callback

abstract class BaseCallbackManager: BaseCallbackHandler {
    abstract fun addHandler(callback: BaseCallbackHandler)
    abstract fun removeHandler(handler: BaseCallbackHandler)
    fun setHandler(handler: BaseCallbackHandler) {
        setHandlers(listOf(handler))
    }
    abstract fun setHandlers(handlers: List<BaseCallbackHandler>)
}