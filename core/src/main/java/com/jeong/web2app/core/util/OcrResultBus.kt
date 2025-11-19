package com.jeong.web2app.core.util

import com.jeong.web2app.core.ocr.OcrResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object OcrResultBus {
    private val _resultFlow = MutableSharedFlow<OcrResult>(replay = 1)
    val resultFlow: SharedFlow<OcrResult> = _resultFlow

    suspend fun post(result: OcrResult) {
        _resultFlow.emit(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        _resultFlow.resetReplayCache()
    }
}
