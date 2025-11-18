package com.jeong.web2app.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object OcrResultBus {

    private val _resultFlow = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 0
    )
    val resultFlow = _resultFlow.asSharedFlow()

    suspend fun post(json: String) {
        _resultFlow.emit(json)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        _resultFlow.resetReplayCache()
    }
}
