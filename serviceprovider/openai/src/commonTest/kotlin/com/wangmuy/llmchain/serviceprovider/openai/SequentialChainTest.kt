package com.wangmuy.llmchain.serviceprovider.openai

import com.wangmuy.llmchain.callback.CallbackManager
import com.wangmuy.llmchain.callback.DefaultCallbackHandler
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.chain.SequentialChain
import com.wangmuy.llmchain.chain.SimpleSequentialChain
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.memory.SimpleMemory
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.schema.LLMResult
import kotlin.test.Test

// https://python.langchain.com/docs/modules/chains/foundational/sequential_chains
class SequentialChainTest {

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

    val llm = OpenAIChat(
        apiKey = OpenAIChatTest.APIKEY,
        baseUrl = OpenAIChatTest.BASEURL,
        timeoutMillis = OpenAIChatTest.TIMEOUT_MILLIS,
        proxy = OpenAIChatTest.PROXY
    ).apply { invocationParams[BaseLLM.REQ_MAX_TOKENS] = 100 }

    @Test fun simpleSequentialChainTest() {
        // This is an LLMChain to write a synopsis given a title of a play.
        val synopsisTemplate = """You are a playwright. Given the title of play, it is your job to write a synopsis for that title in 2 sentences.

Title: {title}

Playwright: This is a synopsis for the above play:"""
        val synopsisPromptTemplate = PromptTemplate(inputVariables = listOf("title"), template = synopsisTemplate)
        val synopsisChain = LLMChain(llm = llm, prompt = synopsisPromptTemplate, outputKey = "synopsis")

        // This is an LLMChain to write a review of a play given a synopsis.
        val template = """You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play in 2 sentences.

Play Synopsis:
{synopsis}

Review from a New York Times play critic of the above play:"""
        val promptTemplate = PromptTemplate(inputVariables = listOf("synopsis"), template = template)
        val reviewChain = LLMChain(llm = llm, prompt = promptTemplate)

        // This is the overall chain where we run these two chains in sequence.
        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        val overallChain = SimpleSequentialChain(
            chains = listOf(synopsisChain, reviewChain),
            callbackManager = callbackManager,
            verbose = true
        )
        val review = overallChain.run(mapOf("title" to "Tragedy at sunset on the beach"))
        println("review=$review")
    }

    @Test fun sequentialChainTest() {
        // This is an LLMChain to write a synopsis given a title of a play and the era it is set in.
        val synopsisTemplate = """You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title in 2 sentences.

Title: {title}
Era: {era}

Playwright: This is a synopsis for the above play:"""
        val synopsisPromptTemplate = PromptTemplate(inputVariables = listOf("title", "era"), template = synopsisTemplate)
        val synopsisChain = LLMChain(llm = llm, prompt = synopsisPromptTemplate, outputKey = "synopsis")

        // This is an LLMChain to write a review of a play given a synopsis.
        val template = """You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play in 2 sentences.

Play Synopsis:
{synopsis}

Review from a New York Times play critic of the above play:"""
        val promptTemplate = PromptTemplate(inputVariables = listOf("synopsis"), template = template)
        val reviewChain = LLMChain(llm = llm, prompt = promptTemplate, outputKey = "review")

        // This is the overall chain where we run these two chains in sequence.
        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        val overallChain = SequentialChain(
            chains = listOf(synopsisChain, reviewChain),
            outputVariables = listOf("synopsis", "review"),
            callbackManager = callbackManager,
            verbose = true
        )
        val output = overallChain.invoke(
            mapOf(
                "title" to "Tragedy at sunset on the beach",
                "era" to "Victorian England"
            )
        )
        println("output=$output")
    }

    @Test fun memoryInSequentialChainTest() {
        // This is an LLMChain to write a synopsis given a title of a play and the era it is set in.
        val synopsisTemplate = """You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title in 2 sentences.

Title: {title}
Era: {era}

Playwright: This is a synopsis for the above play:"""
        val synopsisPromptTemplate = PromptTemplate(inputVariables = listOf("title", "era"), template = synopsisTemplate)
        val synopsisChain = LLMChain(llm = llm, prompt = synopsisPromptTemplate, outputKey = "synopsis")

        // This is an LLMChain to write a review of a play given a synopsis.
        val reviewTemplate = """You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play in 2 sentences.

Play Synopsis:
{synopsis}

Review from a New York Times play critic of the above play:"""
        val reviewPromptTemplate = PromptTemplate(inputVariables = listOf("synopsis"), template = reviewTemplate)
        val reviewChain = LLMChain(llm = llm, prompt = reviewPromptTemplate, outputKey = "review")

        // Memory in Sequential Chains
        val template = """You are a social media manager for a theater company.  Given the title of play, the era it is set in, the date,time and location, the synopsis of the play, and the review of the play, it is your job to write a social media post for that play.

Here is some context about the time and location of the play:
Date and Time: {time}
Location: {location}

Play Synopsis:
{synopsis}

Review from a New York Times play critic of the above play:
{review}

Social Media Post:
"""
        val promptTemplate = PromptTemplate(inputVariables = listOf("synopsis", "review", "time", "location"), template = template)
        val socialChain = LLMChain(llm = llm, prompt = promptTemplate, outputKey = "social_post_text")

        val callbackManager = CallbackManager(mutableListOf(callbackHandler))
        val overallChain = SequentialChain(
            memory = SimpleMemory(memories = mutableMapOf(
                "time" to "December 25th, 8pm PST",
                "location" to "Theater in the Park"
            )),
            chains = listOf(synopsisChain, reviewChain, socialChain),
            callbackManager = callbackManager,
            verbose = true
        )
        val output = overallChain.invoke(mapOf(
            "title" to "Tragedy at sunset on the beach",
            "era" to "Victorian England"
        ))
        println("output=$output")
    }
}