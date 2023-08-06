package com.wangmuy.llmchain.prompt

const val TEMPLATE_FORMAT_FSTRING = "f-string"
private val FSTRING_PATTERN = Regex("\\{\\w+\\}")

fun String.fStringFormat(params: Map<String, Any>?, replaceNotExist: Boolean = true): String {
    if (params == null) {
        return this
    }
    val sb = StringBuilder()
    var result = FSTRING_PATTERN.find(this)
    var lastIndex = 0
    while (result != null) {
        sb.append(this.substring(lastIndex, result.range.first))
        lastIndex = result.range.last + 1
        val keyWithParens = result.value
        val key = keyWithParens.substring(1, keyWithParens.length-1)
        val replaceStr = if (replaceNotExist || key in params)
            params[key].toString()
        else
            keyWithParens
        sb.append(replaceStr)
        result = result.next()
    }
    sb.append(this.substring(lastIndex, this.length))
    return sb.toString()
}
