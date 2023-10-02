package com.wangmuy.llmchain.chain.router

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.chain.Chain
import com.wangmuy.llmchain.schema.BaseMemory

data class Route(
    val destination: String? = null,
    val nextInputs: Map<String, Any> = emptyMap()
)

abstract class RouterChain(
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): Chain(memory, callbackManager, verbose) {
    companion object {
        const val KEY_DESTINATION = "destination"
        const val KEY_NEXT_INPUTS = "next_inputs"
    }

    override fun outputKeys(): List<String> {
        return listOf(KEY_DESTINATION, KEY_NEXT_INPUTS)
    }

    open fun route(inputs: Map<String, Any>, callbackManager: BaseCallbackManager? = null): Route {
        if (callbackManager != null) {
            this.callbackManager = callbackManager
        }
        val result = invoke(inputs)
        return Route(result[KEY_DESTINATION]?.toString(), result[KEY_NEXT_INPUTS] as Map<String, Any>)
    }
}

open class MultiRouteChain(
    var routerChain: RouterChain,
    var destinationChains: Map<String, Chain>,
    var defaultChain: Chain,
    var silentErrors: Boolean = false,
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): Chain(memory, callbackManager, verbose) {

    init {
        setAllCallbackManager(callbackManager)
    }

    private fun setAllCallbackManager(callbackManager: BaseCallbackManager?) {
        super.callbackManager = callbackManager
        routerChain.callbackManager = callbackManager
        destinationChains.forEach { it.value.callbackManager = callbackManager }
        defaultChain.callbackManager = callbackManager
    }

    override var callbackManager: BaseCallbackManager?
        get() = super.callbackManager
        set(value) {
            setAllCallbackManager(value)
        }

    override fun inputKeys(): List<String>? {
        return routerChain.inputKeys()
    }

    override fun outputKeys(): List<String> {
        return emptyList()
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        val route = routerChain.route(inputs!!, callbackManager)
        return if (route.destination.isNullOrEmpty()) {
            defaultChain.invoke(inputs)
        } else if (route.destination in destinationChains) {
            destinationChains[route.destination]!!.invoke(inputs)
        } else if (silentErrors) {
            defaultChain.invoke(inputs)
        } else {
            throw IllegalStateException("Received invalid destination chain name ${route.destination}")
        }
    }
}
