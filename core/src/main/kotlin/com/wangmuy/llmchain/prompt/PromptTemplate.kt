package com.wangmuy.llmchain.prompt

class PromptTemplate @JvmOverloads constructor(
    inputVariables: List<String>,
    private val template: String,
    private val templateFormat: String = TEMPLATE_FORMAT_FSTRING,
    private val validateTemplate: Boolean = true
): StringPromptTemplate(inputVariables) {
    override fun format(args: Map<String, Any>?): String {
        val argStrings = args?.mapValues {entry ->
            when (entry.value) {
                is List<*> -> (entry.value as List<*>).joinToString("\n") { it.toString() }
                else -> entry.value.toString()
            }
        }
        return template.fStringFormat(argStrings)
    }
}