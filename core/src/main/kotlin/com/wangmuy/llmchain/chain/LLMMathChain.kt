package com.wangmuy.llmchain.chain

import com.wangmuy.llmchain.agent.Tool
import com.wangmuy.llmchain.llm.BaseLLM
import com.wangmuy.llmchain.prompt.BasePromptTemplate
import com.wangmuy.llmchain.prompt.PromptTemplate
import com.wangmuy.llmchain.schema.BaseLanguageModel

class LLMMathChain @JvmOverloads constructor(
    val llm: BaseLanguageModel,
    val prompt: BasePromptTemplate = PROMPT,
    private val inputKey: String = KEY_QUESTION,
    private val outputKey: String = "answer"
): Chain() {
    override fun inputKeys(): List<String>? {
        return listOf(inputKey)
    }

    override fun outputKeys(): List<String> {
        return listOf(outputKey)
    }

    private fun procesLLMResult(t: String): Map<String, String> {
        val answerPrefix = "Answer: "
        if (t.contains(answerPrefix)) {
            val answer = t.split(answerPrefix).last().trim()
            return mapOf(outputKey to answer)
        }
        throw IllegalStateException("unknown format from LLM: $t")
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        val llmExecutor = LLMChain(prompt, llm)
        callbackManager?.onText(inputs!![inputKey] as String, verbose)
        val result = llmExecutor.predict(mapOf(
            KEY_QUESTION to inputs!![inputKey]!!
//            "stop" to listOf("```output") // if use actual math expression evaluation
        ))
        return procesLLMResult(result)
    }

    companion object {
        fun asTool(llm: BaseLLM): Tool {
            return Tool(
                "Calculator",
                description = "Useful for when you need to answer questions about math.",
                func = {input -> LLMMathChain(llm).run(mapOf(KEY_QUESTION to input))}
                )
        }

        private const val KEY_QUESTION = "question"
        private const val PROMPT_TEMPLATE = """Translate a math problem into Python code that can be executed in Python 3 REPL. Use the output of running this code to answer the question.

Question: <Question with math problem.>
```python
<Code that solves the problem>
```
```output
<Output of running the code>
```
Answer: <Answer>

Begin.

Question: What is 37593 * 67?

```python
37593 * 67
```
```output
2518731
```
Answer: 2518731

Question: {question}"""
        private val PROMPT = PromptTemplate(listOf(KEY_QUESTION), PROMPT_TEMPLATE)
    }
}