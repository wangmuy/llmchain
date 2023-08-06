package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.agent.Factory
import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.callback.CallbackManager
import com.wangmuy.llmchain.callback.DefaultCallbackHandler
import com.wangmuy.llmchain.chain.ConversationChain
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.chain.LLMMathChain
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.memory.BaseChatMemory
import com.wangmuy.llmchain.prompt.PromptTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// use specific platform to run test(jvmMain/nativeMain/jsMain)
// https://python.langchain.com/en/latest/getting_started/getting_started.html
class Quickstart {

    @Test fun testLLMs() {
        val llm = OpenAIChat(apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.9
        val text = "What would be a good company name for a company that makes colorful socks?"
        val output = llm.invoke(text, null)
        println("output=\n$output")
    }

    @Test fun testPromTemplates() {
        val prompt = PromptTemplate(
            inputVariables = listOf("product"),
            template = "What is a good name for a company that makes {product}?")
        val formatted = prompt.format(mapOf("product" to "colorful socks"))
        assertEquals("What is a good name for a company that makes colorful socks?", formatted)
    }

    @Test fun testChains() {
        val llm = OpenAIChat(apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.9
        val prompt = PromptTemplate(
            inputVariables = listOf("product"),
            template = "What is a good name for a company that makes {product}?")
        val chain = LLMChain(llm = llm, prompt = prompt)
        val output = chain.run(mapOf("product" to "colorful socks"))
        println("output=\n$output")
    }

    @Test fun testAgents() {
        val llm = OpenAIChat(apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val fakeSerpApiTool = Tool(
            name = "Search",
            description = "A search engine. Useful for when you need to answer questions about current events. Input should be a search query.",
            func = {_, _ -> "San Francisco Temperature Yesterday. Maximum temperature yesterday: 57 °F (at 1:56 pm) Minimum temperature yesterday: 49 °F (at 1:56 am)"}
        )
        val llmMathTool = LLMMathChain.asTool(llm)
        val agentExecutor = Factory.initializeAgent(listOf(fakeSerpApiTool, llmMathTool), llm,
            Factory.AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION)
        agentExecutor.maxIterations = 4
        val output = agentExecutor.run(mapOf("input" to "What was the high temperature in SF yesterday in Fahrenheit? What is that number raised to the .023 power?"))
        println("output=\n$output")
    }

    @Test fun testMemory() {
        val logCallbackHandler = object: DefaultCallbackHandler() {
            override fun onText(text: String, verbose: Boolean) {
                println(text)
            }
        }
        val callbackManager = CallbackManager(mutableListOf(logCallbackHandler))
        val llm  = OpenAIChat(apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy).apply {
            invocationParams[BaseLLM.REQ_MAX_TOKENS] = 50
        }
        llm.callbackManager = callbackManager
        val conversation = ConversationChain(llm, verbose = true, callbackManager = callbackManager)
        var output: Map<String, Any> = emptyMap()
        var outputStr: String = ""
        output = conversation.invoke(mapOf("input" to "Hi there!"))
        outputStr = output[conversation.outputKey]!!.toString()
        println("output=\n$outputStr")
        output = conversation.invoke(mapOf("input" to "I'm doing well! Just having a conversation with an AI."))
//        assertEquals(output[conversation.inputKey])
        outputStr = output[conversation.outputKey]!!.toString()
        println("output=\n$output")
        assertTrue(conversation.memory is BaseChatMemory)
        assertEquals(4, (conversation.memory as BaseChatMemory).chatMemory.messages.size)
    }
}