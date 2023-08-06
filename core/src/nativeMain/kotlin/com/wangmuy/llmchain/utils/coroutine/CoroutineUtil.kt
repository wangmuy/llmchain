package com.wangmuy.llmchain.utils.coroutine

import kotlinx.coroutines.runBlocking

actual fun <T> runBlockingKMP(block: suspend () -> T): T = runBlocking { block() }