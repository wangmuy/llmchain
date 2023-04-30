package com.wangmuy.llmchain

import com.wangmuy.llmchain.agent.Factory
import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.chain.ConversationChain
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.chain.LLMMathChain
import com.wangmuy.llmchain.llm.openai.OpenAIChat
import com.wangmuy.llmchain.memory.BaseChatMemory
import com.wangmuy.llmchain.prompt.PromptTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Proxy

// https://python.langchain.com/en/latest/getting_started/getting_started.html
class QuickstartModel {
    companion object {
        private const val APIKEY = "sk-"
        private val PROXY = null //Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 80))
    }

    @Test fun testLLMs() {
        val llm = OpenAIChat(APIKEY, proxy = PROXY)
        llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.9
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
        val llm = OpenAIChat(APIKEY)
        llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.9
        val prompt = PromptTemplate(
            inputVariables = listOf("product"),
            template = "What is a good name for a company that makes {product}?")
        val chain = LLMChain(llm = llm, prompt = prompt)
        val output = chain.run(mapOf("product" to "colorful socks"))
        println("output=\n$output")
    }

    @Test fun testAgents() {
        val llm = OpenAIChat(APIKEY)
        llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.0
        val fakeSerpApiTool = Tool(
            name = "Search",
            description = "A search engine. Useful for when you need to answer questions about current events. Input should be a search query.",
            func = {_ -> "San Francisco Temperature Yesterday. Maximum temperature yesterday: 57 °F (at 1:56 pm) Minimum temperature yesterday: 49 °F (at 1:56 am)"}
        )
        val llmMathTool = LLMMathChain.asTool(llm)
        val agentExecutor = Factory.initializeAgent(listOf(fakeSerpApiTool, llmMathTool), llm,
            Factory.AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION)
        agentExecutor.maxIterations = 4
        val output = agentExecutor.run(mapOf("input" to "What was the high temperature in SF yesterday in Fahrenheit? What is that number raised to the .023 power?"))
        println("output=\n$output")
    }

    @Test fun testMemory() {
        val llm  = OpenAIChat(APIKEY)
        val conversation = ConversationChain(llm)
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