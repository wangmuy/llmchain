package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import kotlin.test.Test

// use specific platform to run test(jvmMain/nativeMain/jsMain)
class OpenAIChatTest {
    companion object {
        const val APIKEY = openAIKey
        const val BASEURL = openAIBaseUrl
        const val TIMEOUT_MILLIS: Long = openAITimeoutMillis
        val PROXY = openAIProxy
    }

    @Test fun testOpenAIChat() {
        val llm = OpenAIChat(
            apiKey = openAIKey, baseUrl = openAIBaseUrl,
            timeoutMillis = openAITimeoutMillis, proxy = openAIProxy)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = Clock.System.now().toEpochMilliseconds()
        val output = llm.invoke("Say foo:", null)
        val costMillis = Clock.System.now().toEpochMilliseconds() - begMillis
        println("costMillis=$costMillis, output=$output")
    }

    @Test fun testOpenAIChatFunction() {
        val llm = OpenAIChat(apiKey = APIKEY, baseUrl = BASEURL, timeoutMillis = TIMEOUT_MILLIS, proxy = PROXY)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val tools = listOf(
            object: BaseTool(
                name = "currentWeather",
                description = "Get the current weather in a given location") {
                override fun onRun(toolInput: String, args: Map<String, Any>?): String {
                    println("onRun toolInput=$toolInput, args=$args")
                    return "run success"
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
                            add("unit")
                        }
                    }
                }
            }
        )
        val inputs = listOf(mapOf(FunctionUtil.KEY_FUNCTION_CALL to tools))
        val begMillis = Clock.System.now().toEpochMilliseconds()
        val output = llm.generate(listOf("What's the weather in SF?"), null, inputs)
        val costMillis = Clock.System.now().toEpochMilliseconds() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}