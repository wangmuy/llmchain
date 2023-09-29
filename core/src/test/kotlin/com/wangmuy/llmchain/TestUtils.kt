package com.wangmuy.llmchain

import java.io.File
import java.util.Properties


fun String.resourceAsText(): String? {
    return this::class.java.getResource(this)?.readText()
}

fun String.resourceAsProperties(): Properties {
    val prop = Properties()
    try {
        this::class.java.getResourceAsStream(this).use {
            prop.load(it)
        }
    } catch (e: Exception) {
        println("warning: read resources properties $this failed: ${e.stackTraceToString()}")
    }
    return prop
}

fun String.filePathAsText(): String {
    return File(this).readText()
}

fun String.filePathAsProperties(): Properties {
    val prop = Properties()
    try {
        File(this).inputStream().use {
            prop.load(it)
        }
    } catch (e: Exception) {
        println("warning: read properties $this failed: ${e.stackTraceToString()}")
    }
    return prop
}