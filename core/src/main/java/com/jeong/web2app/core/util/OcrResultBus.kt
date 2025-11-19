package com.jeong.web2app.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object OcrResultBus {
    private val _resultFlow = MutableSharedFlow<String>(replay = 1)
    val resultFlow: SharedFlow<String> = _resultFlow

    suspend fun post(json: String) {
        _resultFlow.emit(json)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        _resultFlow.resetReplayCache()
    }
}
