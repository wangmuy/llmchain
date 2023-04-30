package com.wangmuy.llmchain.agent

import com.wangmuy.llmchain.callback.BaseCallbackManager
import com.wangmuy.llmchain.chain.Chain
import com.wangmuy.llmchain.schema.AgentAction
import com.wangmuy.llmchain.schema.AgentFinish
import com.wangmuy.llmchain.schema.BaseAgentAction
import com.wangmuy.llmchain.tool.BaseTool
import java.util.*

class AgentExecutor @JvmOverloads constructor(
    val agent: com.wangmuy.llmchain.agent.BaseAgent,
    val tools: List<BaseTool>,
    callbackManager: BaseCallbackManager?,
    var returnIntermediateSteps: Boolean = false,
    var maxIterations: Int? = 15,
    var maxExecutionTime: Long? = null,
    var earlyStoppingMethod: String = "force"
): Chain(callbackManager = callbackManager) {
    override fun inputKeys(): List<String>? {
        return agent.inputKeys()
    }

    override fun outputKeys(): List<String> {
        return if (returnIntermediateSteps) {
            agent.returnValues().toMutableList().also { it.add("intermediate_steps") }
        } else {
            agent.returnValues()
        }
    }

    fun lookupTool(name: String): BaseTool? {
        return tools.find { name == it.name }
    }

    private fun shouldContinue(iterations: Int, timeElapsed: Long): Boolean {
        if (maxIterations != null && iterations > maxIterations!!) {
            return false
        }
        return maxExecutionTime == null || timeElapsed < maxExecutionTime!!
    }

    private fun onReturn(output: AgentFinish, intermediateSteps: List<com.wangmuy.llmchain.agent.IntermediateStep>)
    : Map<String, Any> {
        callbackManager?.onAgentFinish(output, verbose)
        val finalOutput = output.returnValues
        return if (returnIntermediateSteps)
            finalOutput.toMutableMap().also { it["intermediate_steps"] = intermediateSteps }
        else
            finalOutput
    }

    private fun takeNextStep(
        nameToToolMap: Map<String, BaseTool>,
        inputs: Map<String, Any>?,
        intermediateSteps: List<com.wangmuy.llmchain.agent.IntermediateStep>): List<BaseAgentAction> {
        val output = agent.plan(intermediateSteps, inputs)
        if (output is AgentFinish) {
            return Collections.singletonList(output)
        }
        val actions = listOf(output as AgentAction)
        val result = mutableListOf<com.wangmuy.llmchain.agent.IntermediateStep>()
        for (agentAction in actions) {
            callbackManager?.onAgentAction(agentAction, verbose)
            val observation = if (nameToToolMap.containsKey(agentAction.tool)) {
                val tool = nameToToolMap[agentAction.tool]!!
                var toolRunArgs = agent.toolRunLoggingArgs()
                if (tool.returnDirect) {
                    toolRunArgs = toolRunArgs.toMutableMap().also { it["llm_prefix"] = "" }
                }
                tool.run(agentAction.toolInput, this.verbose, toolRunArgs)
            } else {
                val toolRunArgs = agent.toolRunLoggingArgs()
                InvalidTool().run(agentAction.tool, this.verbose, toolRunArgs)
            }
            result.add(com.wangmuy.llmchain.agent.IntermediateStep(agentAction, observation))
        }
        return result
    }

    override fun onInvoke(inputs: Map<String, Any>?): Map<String, Any> {
        val nameToToolMap = tools.associateBy { it.name }
        val intermediateSteps = mutableListOf<com.wangmuy.llmchain.agent.IntermediateStep>()
        var iterations = 0
        var timeElapsed = 0L
        val startTime = System.currentTimeMillis()
        while (shouldContinue(iterations, timeElapsed)) {
            val nextStepOutput = takeNextStep(nameToToolMap, inputs, intermediateSteps)
            if (nextStepOutput.size == 1 && nextStepOutput[0] is AgentFinish) {
                return onReturn(nextStepOutput[0] as AgentFinish, intermediateSteps)
            }

            intermediateSteps.addAll(nextStepOutput as List<com.wangmuy.llmchain.agent.IntermediateStep>)
            if (nextStepOutput.size == 1) {
                val nextStepAction = nextStepOutput[0]
                val toolReturn = getToolReturn(nextStepAction)
                if (toolReturn != null) {
                    return onReturn(toolReturn, intermediateSteps)
                }
            }
            iterations += 1
            timeElapsed = System.currentTimeMillis() - startTime
        }
        val output = agent.returnStoppedResponse(earlyStoppingMethod, intermediateSteps, inputs)
        return onReturn(output, intermediateSteps)
    }

    private fun getToolReturn(nextStepOutput: com.wangmuy.llmchain.agent.IntermediateStep): AgentFinish? {
        val agentAction = nextStepOutput.action
        val observation = nextStepOutput.observation
        val tool = this.tools.find { it.name == agentAction.tool }
        if (tool != null && tool.returnDirect) {
            return AgentFinish(mapOf(agent.returnValues()[0] to observation), "")
        }
        return null
    }
}