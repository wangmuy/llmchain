package com.wangmuy.llmchain.utils.time

import kotlinx.datetime.Clock

actual fun currentTimeMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}