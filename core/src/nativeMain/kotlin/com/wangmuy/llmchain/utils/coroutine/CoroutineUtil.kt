package com.wangmuy.llmchain.utils.coroutine

import kotlinx.coroutines.runBlocking

actual fun runBlockingKMP(block: suspend () -> Unit) = runBlocking { block() }