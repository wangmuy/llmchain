package com.wangmuy.llmchain.callback

import java.util.*

abstract class BaseCallbackManager: BaseCallbackHandler {
    abstract fun addHandler(callback: BaseCallbackHandler)
    abstract fun removeHandler(handler: BaseCallbackHandler)
    open fun setHandler(handler: BaseCallbackHandler) {
        setHandlers(Collections.singletonList(handler))
    }
    abstract fun setHandlers(handlers: List<BaseCallbackHandler>)
}