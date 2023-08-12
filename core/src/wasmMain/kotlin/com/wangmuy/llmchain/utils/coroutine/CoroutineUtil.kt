package com.wangmuy.llmchain.utils.coroutine

actual fun <T> runBlockingKMP(block: suspend () -> T): T = run { TODO() }