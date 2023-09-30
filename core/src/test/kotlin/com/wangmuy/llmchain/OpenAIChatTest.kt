package com.wangmuy.llmchain

import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.llm.FunctionUtil
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import com.wangmuy.llmchain.tool.BaseTool
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Proxy

class OpenAIChatTest {
    companion object {
        private val props = "src/test/resources/private.properties".filePathAsProperties()
        val BASEURL = props.getProperty("BASEURL")
        val APIKEY = props.getProperty("APIKEY")
        val PROXY: String? = null//"socks5://127.0.0.1:1090"
        val TIMEOUT_MILLIS: Long = 60000
    }

    @Test fun testOpenAIChat() {
        val llm = OpenAIChat(apiKey = APIKEY, baseUrl = BASEURL, timeoutMillis = TIMEOUT_MILLIS, proxy = PROXY)
        llm.invocationParams[BaseLLM.REQ_MODEL_NAME] = "gpt-3.5-turbo"//"gpt-3.5-turbo"// "fastchat-t5-3b-v1.0"
        llm.invocationParams[BaseLLM.REQ_MAX_TOKENS] = 40
//        llm.invocationParams[BaseLLM.REQ_TEMPERATURE] = 0.0
        val begMillis = System.currentTimeMillis()
        val output = llm.invoke("Say foo:", null)
        val costMillis = System.currentTimeMillis() - begMillis
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
        val begMillis = System.currentTimeMillis()
        val output = llm.generate(listOf("What's the weather in SF?"), null, inputs)
        val costMillis = System.currentTimeMillis() - begMillis
        println("costMillis=$costMillis, output=$output")
    }
}