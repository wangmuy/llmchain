package com.wangmuy.llmchain.utils.coroutine

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

@OptIn(DelicateCoroutinesApi::class)
actual fun <T> runBlockingKMP(block: suspend () -> T): dynamic = GlobalScope.promise { block() }