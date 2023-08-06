package com.wangmuy.llmchain.utils.coroutine

expect fun <T> runBlockingKMP(block: suspend () -> T): T