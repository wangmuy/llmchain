package com.wangmuy.llmchain.utils.coroutine

actual fun runBlockingKMP(block: suspend () -> Unit): dynamic = run { }