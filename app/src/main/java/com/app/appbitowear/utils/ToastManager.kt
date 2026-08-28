package com.app.appbitowear.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ToastManager {
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun showToast(msg: String) {
        _message.value = msg
    }

    fun clearToast() {
        _message.value = null
    }
}