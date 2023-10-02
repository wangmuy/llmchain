package com.wangmuy.llmchain

import com.wangmuy.llmchain.agent.router.LLMRouterChain
import com.wangmuy.llmchain.agent.router.MultiPromptChain
import com.wangmuy.llmchain.agent.router.RouterOutputParser
import com.wangmuy.llmchain.callback.CallbackManager
import com.wangmuy.llmchain.callback.DefaultCallbackHandler
import com.wangmuy.llmchain.chain.ConversationChain
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.prompt.fStringFormat
import com.wangmuy.llmchain.schema.BaseOutputParser
import com.wangmuy.llmchain.schema.LLMResult
import com.wangmuy.llmchain.serviceprovider.openai.OpenAIChat
import org.junit.Test

class RouterChainTest {
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

    val physicsTemplate = """You are a very smart physics professor. You are great at answering questions about physics in a concise and easy to understand manner. When you don't know the answer to a question you admit that you don't know.

Here is a question:
{input}"""
    val mathTemplate = """You are a very good mathematician. You are great at answering math questions. You are so good because you are able to break down hard problems into their component parts, answer the component parts, and then put them together to answer the broader question.

Here is a question:
{input}"""
    val promptInfos = listOf(
        mapOf(
            "name" to "physics",
            "description" to "Good for answering questions about physics",
            "prompt_template" to physicsTemplate
        ),
        mapOf(
            "name" to "math",
            "description" to "Good for answering math questions",
            "prompt_template" to mathTemplate
        )
    )

    val llm = OpenAIChat(
        apiKey = OpenAIChatTest.APIKEY,
        baseUrl = OpenAIChatTest.BASEURL,
        timeoutMillis = OpenAIChatTest.TIMEOUT_MILLIS,
        proxy = OpenAIChatTest.PROXY
    )

    // https://python.langchain.com/docs/modules/chains/foundational/router
    @Test fun testLLMRouterChain() {
        val destinationChains = mutableMapOf<String, LLMChain>()
        for (info in promptInfos) {
            val name = info["name"]!!
            val promptTemplate = info["prompt_template"]!!
            val prompt = PromptTemplate(template = promptTemplate, inputVariables = listOf("input"))
            val chain = LLMChain(llm = llm, prompt = prompt)
            destinationChains[name] = chain
        }
        val defaultChain = ConversationChain(llm = llm, outputKey = "text")

        val destinations = promptInfos.map { "${it["name"]}: ${it["description"]}" }
        val destinationsStr = destinations.joinToString("\n")
        val routerTemplate = MultiPromptChain.MULTI_PROMPT_ROUTER_TEMPLATE.fStringFormat(
            mapOf("destinations" to destinationsStr), replaceNotExist = false)
        val routerPrompt = PromptTemplate(
            template = routerTemplate,
            inputVariables = listOf("input"),
            outputParser = RouterOutputParser() as BaseOutputParser<Any>)
        val routerChain = LLMRouterChain(LLMChain(llm = llm, prompt = routerPrompt))

        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        val chain = MultiPromptChain(
            routerChain = routerChain,
            destinationChains = destinationChains,
            defaultChain = defaultChain,
            verbose = true,
            callbackManager = callbackManager
        )
        val output = chain.run(mapOf("input" to "What is black body radiation?"))
        println("output=$output")
    }

    @Test fun testMultiPromptChain() {
        val chain = MultiPromptChain.fromPrompts(llm = llm, promptInfos = promptInfos)
        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        chain.callbackManager = callbackManager
        val output = chain.run(mapOf("input" to "What is black body radiation?"))
        println("output=$output")
    }
}