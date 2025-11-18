package com.jeong.web2app.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object OcrResultBus {
    private val _resultFlow = MutableSharedFlow<String>()
    val resultFlow: SharedFlow<String> = _resultFlow

    suspend fun post(json: String) {
        _resultFlow.emit(json)
    }
}
