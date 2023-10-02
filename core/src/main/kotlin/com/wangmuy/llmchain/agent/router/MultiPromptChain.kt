package com.wangmuy.llmchain.agent.router

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.chain.ConversationChain
import com.wangmuy.llmchain.chain.LLMChain
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.prompt.fStringFormat
import com.wangmuy.llmchain.schema.BaseLanguageModel
import com.wangmuy.llmchain.schema.BaseMemory
import com.wangmuy.llmchain.schema.BaseOutputParser

class MultiPromptChain(
    routerChain: RouterChain,
    destinationChains: Map<String, LLMChain>,
    defaultChain: LLMChain,
    silentErrors: Boolean = false,
    memory: BaseMemory? = null,
    callbackManager: BaseCallbackManager? = null,
    verbose: Boolean = false
): MultiRouteChain(
    routerChain, destinationChains, defaultChain, silentErrors,
    memory, callbackManager, verbose) {

    companion object {
        const val KEY_INPUT_TEXT = "text"

        fun fromPrompts(
            llm: BaseLanguageModel,
            promptInfos: List<Map<String, String>>,
            defaultChain: LLMChain? = null,
            inputVariables: List<String> = listOf("input")
        ): MultiPromptChain {
            val destinations = promptInfos.map { "${it["name"]}: ${it["description"]}" }
            val destinationsStr = destinations.joinToString("\n")
            val routerTemplate = MULTI_PROMPT_ROUTER_TEMPLATE.fStringFormat(mapOf("destinations" to destinationsStr), replaceNotExist = false)
            val routerPrompt = PromptTemplate(
                template = routerTemplate,
                inputVariables = inputVariables,
                outputParser = RouterOutputParser() as BaseOutputParser<Any>
            )
            val routerChain = LLMRouterChain(LLMChain(llm = llm, prompt = routerPrompt))
            val destinationChains = mutableMapOf<String, LLMChain>()
            for (info in promptInfos) {
                val name = info["name"]!!
                val promptTemplate = info["prompt_template"]!!
                val prompt = PromptTemplate(
                    template = promptTemplate, inputVariables = inputVariables)
                val chain = LLMChain(llm = llm, prompt = prompt)
                destinationChains[name] = chain
            }
            val defChain = defaultChain ?: ConversationChain(llm = llm, outputKey = KEY_INPUT_TEXT)
            return MultiPromptChain(
                routerChain = routerChain,
                destinationChains = destinationChains,
                defaultChain = defChain
            )
        }

        const val MULTI_PROMPT_ROUTER_TEMPLATE = """Given a raw text input to a language model select the model prompt best suited for the input. You will be given the names of the available prompts and a description of what the prompt is best suited for. You may also revise the original input if you think that revising it will ultimately lead to a better response from the language model.

<< FORMATTING >>
Return a markdown code snippet with a JSON object formatted to look like:
```json
{
    "destination": string \\ name of the prompt to use or "DEFAULT"
    "next_inputs": string \\ a potentially modified version of the original input
}
```

REMEMBER: "destination" MUST be one of the candidate prompt names specified below OR it can be "DEFAULT" if the input is not well suited for any of the candidate prompts.
REMEMBER: "next_inputs" can just be the original input if you don't think any modifications are needed.

<< CANDIDATE PROMPTS >>
{destinations}

<< INPUT >>
{input}

<< OUTPUT (must include ```json at the start of the response) >>
<< OUTPUT (must end with ```) >>
"""
    }

    init {
        if (callbackManager != null) {
            routerChain.callbackManager = callbackManager
            destinationChains.forEach { (_, llmChain) -> llmChain.callbackManager = callbackManager }
            defaultChain.callbackManager = callbackManager
        }
    }

    override fun outputKeys(): List<String> {
        return listOf(KEY_INPUT_TEXT)
    }
}