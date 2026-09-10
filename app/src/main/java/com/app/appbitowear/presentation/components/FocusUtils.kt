package com.app.appbitowear.presentation.components

import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester

fun handleFocus(
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    action: () -> Unit = {}
) {
    focusManager.clearFocus()
    focusRequester.requestFocus()
    action()
}