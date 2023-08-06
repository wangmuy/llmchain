package com.wangmuy.llmchain

import com.wangmuy.llmchain.agent.AgentExecutor
import com.wangmuy.llmchain.agent.Factory.Companion.AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION
import com.wangmuy.llmchain.agent.Factory.Companion.initializeAgent
import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.llm.LLM
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeListLLM(
    private val responses: List<String>
): LLM() {
    private var i = -1

    override fun onInvoke(prompt: String, stop: List<String>?): String {
        this.i += 1
        println("=== Mock Response ${this.i} ===")
        if (i >= 2) {
            throw IllegalStateException("maxIter i==$i")
        }
        val idx = if (i>=2) 1 else this.i
        println(responses[idx])
        return responses[idx]
    }
}

class AgentTest {
    private fun getAgent(args: Map<String, Any>?): AgentExecutor {
        val badActionName = "BadAction"
        val responses = listOf(
            "I'm turning evil\nAction: $badActionName\nAction Input: misalignment",
            "Oh well\nAction: Final Answer\nAction Input: curses foiled again"
        )
        val fakeLLM = FakeListLLM(responses)
        val tools = listOf(
            Tool("Search", {x, _ -> x},  "Useful for searching"),
            Tool("Lookup", {x, _ -> x}, "Useful for looking up things in a table")
        )
        return initializeAgent(tools, fakeLLM, AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION, null, args)
    }

    @Test fun testAgentBadAction() {
        val agent = getAgent(null)
        val output = agent.run(mapOf("input" to "when was langchain made"))
        assertEquals("curses foiled again", output)
    }

    @Test fun testAgentLookupTool() {
        val fakeLLM = FakeListLLM(listOf(
            "FooBarBaz\nAction: Search\nAction Input: misalignment"
        ))
        val tools = listOf(
            Tool("Search", {x, _ -> x}, "Useful for searching", true)
        )
        val agent = initializeAgent(tools, fakeLLM, AGENT_TYPE_ZERO_SHOT_REACT_DESCRIPTION)
        assertEquals(tools[0], agent.lookupTool("Search"))
    }
}