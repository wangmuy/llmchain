package com.wangmuy.llmchain

import com.wangmuy.llmchain.OpenAIChatTest.Companion.APIKEY
import com.wangmuy.llmchain.OpenAIChatTest.Companion.BASEURL
import com.wangmuy.llmchain.OpenAIChatTest.Companion.PROXY
import com.wangmuy.llmchain.OpenAIChatTest.Companion.TIMEOUT_MILLIS
import com.wangmuy.llmchain.agent.AgentExecutor
import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.agent.ZeroShotAgent
import com.wangmuy.llmchain.callback.CallbackManager
import com.wangmuy.llmchain.callback.DefaultCallbackHandler
import com.wangmuy.llmchain.chain.LLMMathChain
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.Test

class ZeroShotAgentTest {
    private val callbackHandler = object: DefaultCallbackHandler() {
        override fun alwaysVerbose(): Boolean {
            return true
        }

        override fun onText(text: String, verbose: Boolean) {
            println(text)
        }

        override fun onLLMEnd(response: LLMResult, verbose: Boolean) {
            println("outputs=<<<<<")
            println(response.generations[0][0].text)
            println(">>>>>")
        }
    }

    @Test fun simpleTest() {
        val llm = OpenAIChat(
            apiKey = APIKEY,
            baseUrl = BASEURL,
            timeoutMillis = TIMEOUT_MILLIS,
            proxy = PROXY
        )
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 100
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val fakeSerpApiTool = Tool(
            name = "Search",
            description = "A search engine. Useful for when you need to answer questions about current events. Input should be a search query.",
            func = {_, _ -> "San Francisco Temperature Yesterday. Maximum temperature yesterday: 57 °F (at 1:56 pm) Minimum temperature yesterday: 49 °F (at 1:56 am)"}
        )
        val llmMathTool = LLMMathChain.asTool(llm)
        val tools = listOf(fakeSerpApiTool, llmMathTool)
        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        val agent = ZeroShotAgent.Builder().llm(llm).tools(tools).also {
            it.callbackManager(callbackManager)
        }.build()
        val agentExecutor = AgentExecutor(agent, tools, callbackManager)
        agentExecutor.maxIterations = 4
        val output = agentExecutor.run(mapOf("input" to "What was the high temperature in SF yesterday in Fahrenheit? What is that number raised to the .023 power?"))
        println("output=\n$output")
    }

    private fun getToolsAndQuestionQuickStart(llm: BaseLLM): Pair<List<BaseTool>, String> {
        val fakeSerpApiTool = Tool(
            name = "Search",
            description = "A search engine. Useful for when you need to answer questions about current events. Input should be a search query.",
            func = {_, _ -> "San Francisco Temperature Yesterday. Maximum temperature yesterday: 57 °F (at 1:56 pm) Minimum temperature yesterday: 49 °F (at 1:56 am)"}
        )
        val llmMathTool = LLMMathChain.asTool(llm)
        val tools = listOf(fakeSerpApiTool, llmMathTool)
        return Pair(tools, "What was the high temperature in SF yesterday in Fahrenheit? What is that number raised to the .023 power?")
    }

    private fun getToolsAndQuestionWeather(llm: BaseLLM): Pair<List<BaseTool>, String> {
        val weatherTool = object : BaseTool(
            name = "get_current_weather",
            description = "Get the current weather in a given location",
        ) {
            override fun onRun(toolInput: String, args: Map<String, Any>?): String {
                println("onRun toolInput=$toolInput, args=$args")
                return "Current temperature is 35 celsius"
            }

            override fun parameterSchema(): JsonObject {
                return buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("location") {
                            put("type", "string")
                            put("description", "The city and state, e.g. San Francisco, CA")
                        }
                        putJsonObject("unit") {
                            put("type", "string")
                            putJsonArray("enum") {
                                add("celsius")
                                add("fahrenheit")
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("location")
                    }
                }
            }
        }
        val tools = listOf(weatherTool)
        return Pair(tools, "What is the weather like in Boston?")
    }

    @Test fun functionTest() {
        val llm = OpenAIChat(
            apiKey = APIKEY,
            baseUrl = BASEURL,
            timeoutMillis = TIMEOUT_MILLIS,
            proxy = PROXY
        )
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 100
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0

        val (tools, query) = getToolsAndQuestionQuickStart(llm)//getToolsAndQuestionWeather(llm)
        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        // 1. disable buildToolStrings
        val promptBuilder = object: ZeroShotAgent.PromptBuilder() {
//            override fun buildToolStrings(): String {
//                return ""
//            }
        }.also {
            it.prefix = "Answer the following questions as best you can."
            // since function calling is used, we can remove "Action" and "Action Input" from the format instructions
            // CAUTION: not removing these may result in not making function calls
            it.formatInstructions = """Use the following format:

Question: the input question you must answer
Thought: you should always think about what to do
Observation: the result of the action
... (this Thought/Observation can repeat N times)
Thought: I now know the final answer
Final Answer: the final answer to the original input question"""
        }
        val agentBuilder = ZeroShotAgent.Builder().llm(llm).tools(tools).also {
            it.callbackManager(callbackManager)
            it.promptBuilder = promptBuilder
        }
        val agent = agentBuilder.build()
        val agentExecutor = AgentExecutor(agent, tools, callbackManager)
        agentExecutor.maxIterations = 4
        // 2. add function call tools input
        val output = agentExecutor.run(mapOf(
            "input" to query,
            FunctionUtil.KEY_FUNCTION_CALL to tools
        ))
        println("output=$output")
    }
}