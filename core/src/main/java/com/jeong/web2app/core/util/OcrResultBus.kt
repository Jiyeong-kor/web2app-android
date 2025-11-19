package com.jeong.web2app.core.util

import com.jeong.web2app.core.ocr.OcrResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object OcrResultBus {

    private val _resultFlow = MutableSharedFlow<OcrResult>(
        replay = 1,
        extraBufferCapacity = 0
    )
    val resultFlow = _resultFlow.asSharedFlow()

    suspend fun post(result: OcrResult) {
        _resultFlow.emit(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        _resultFlow.resetReplayCache()
    }
}
