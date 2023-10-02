# LLMChain
This is an experimental port of [langchain( currently v0.0.139 )](https://github.com/hwchase17/langchain/tree/v0.0.139) to the Kotlin/JVM ecosystem.
Please note that this project is currently in the proof-of-concept stage,
and its API is subject to change.

## Maven repository
Only the SNAPSHOT version is published.

### Add to repositories
```gradle
repositories {
    mavenCentral()
    maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
}
```

### Add to dependencies
build.gradle
```gradle
dependencies {
    implementation("io.github.wangmuy.llmchain:core:0.0.1-SNAPSHOT") { changing=true }
}
```

build.gradle.kts
```kotlin
implementation("io.github.wangmuy.llmchain:core:0.0.1-SNAPSHOT")
configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
```

## Quickstart
Here's the almost one-to-one translation of  [langchain Quickstart Guide](https://python.langchain.com/docs/get_started/quickstart)
 in [Quickstart.kt](https://github.com/wangmuy/llmchain/blob/main/core/src/test/kotlin/com/wangmuy/llmchain/Quickstart.kt), including all the modules/components.

### LLMs
```kotlin
val llm = OpenAIChat(APIKEY, proxy = PROXY)
llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.9
val text = "What would be a good company name for a company that makes colorful socks?"
val output = llm.invoke(text, null)
println("output=\n$output")
```

### Prompt templates
```kotlin
val prompt = PromptTemplate(
    inputVariables = listOf("product"),
    template = "What is a good name for a company that makes {product}?")
val formatted = prompt.format(mapOf("product" to "colorful socks"))
assertEquals("What is a good name for a company that makes colorful socks?", formatted)
```

### Chains
```kotlin
val llm = OpenAIChat(APIKEY)
llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.9
val prompt = PromptTemplate(
    inputVariables = listOf("product"),
    template = "What is a good name for a company that makes {product}?")
val chain = LLMChain(llm = llm, prompt = prompt)
val output = chain.run(mapOf("product" to "colorful socks"))
println("output=\n$output")
```

### Agents
```kotlin
val llm = OpenAIChat(APIKEY)
llm.invocationParams[OpenAIChat.REQ_TEMPERATURE] = 0.0
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
```

### Memory
```kotlin
val logCallbackHandler = object: DefaultCallbackHandler() {
    override fun onText(text: String, verbose: Boolean) {
        println(text)
    }
}
val callbackManager = CallbackManager(mutableListOf(logCallbackHandler))
val llm  = OpenAIChat(APIKEY).apply {
    invocationParams[OpenAIChat.REQ_MAX_TOKENS] = 50
}
llm.callbackManager = callbackManager
val conversation = ConversationChain(llm, verbose = true, callbackManager = callbackManager)
var output: Map<String, Any> = emptyMap()
var outputStr: String = ""
output = conversation.invoke(mapOf("input" to "Hi there!"))
outputStr = output[conversation.outputKey]!!.toString()
println("output=\n$outputStr")
output = conversation.invoke(mapOf("input" to "I'm doing well! Just having a conversation with an AI."))
```

## implementations
- Schema
  - [x] nearly all basic schema interfaces
- LLM
  - [x] BaseLLM
  - [x] LLM
- Prompt template
  - [x] BasePromptTemplate
  - [x] StringPromptTemplate
  - [x] StringPromptValue
  - [x] PromptTemplate
- Chain
  - [x] Chain
  - [x] LLMChain
  - [ ] LLMMathChain: currently use LLM, not the actual calculator
  - [x] ConversationChain
  - [x] RouterChain/MultiRouteChain/LLMRouterChain/MultiPromptChain
  - [x] SequentialChain/SimpleSequentialChain
- Agent
  - [x] Agent
  - [x] AgentExecutor
  - [x] ZeroShotAgent
- Tools
  - [x] BaseTool
  - [x] Tool
  - [x] InvalidTool
- Memory
  - [x] SimpleMemory
  - [x] ChatMemory
- DocStore
  - [x] DocStore
  - [x] InMemoryDocStore
- VectorStore
  - [x] VectorStore
  - [x] SimpleVectorStore
  - [x] VectorStoreRetriever
- Embedding
  - [x] Embeddings
- OpenAI
  - [x] OpenAIChat
  - [x] OpenAIEmbedding


- LLM service provider
  - [x] OpenAI
  - [x] [FastChat OpenAI-compatible restful apis](https://github.com/lm-sys/FastChat/blob/main/docs/openai_api.md)
  - [ ] GPT4All: java bindings for desktops

## License
```text
Copyright 2023 wangmuy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
