package com.wangmuy.llmchain.prompt

import java.util.regex.Pattern

const val TEMPLATE_FORMAT_FSTRING = "f-string"
private val FSTRING_PATTERN = Pattern.compile("\\{\\w+\\}")

fun String.fStringFormat(params: Map<String, Any>?, replaceNotExist: Boolean = true): String {
    if (params == null) {
        return this
    }
    val matcher = FSTRING_PATTERN.matcher(this)
    val sb = StringBuffer()
    while (matcher.find()) {
        val keyWithParens = matcher.group()
        val key = keyWithParens.substring(1, keyWithParens.length-1)
        val replaceStr = if (replaceNotExist || key in params)
            params[key].toString()
        else
            keyWithParens
        matcher.appendReplacement(sb, replaceStr)
    }
    matcher.appendTail(sb)
    return sb.toString()
}
