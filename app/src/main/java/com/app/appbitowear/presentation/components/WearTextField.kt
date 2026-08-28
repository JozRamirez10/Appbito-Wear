package com.app.appbitowear.presentation.components

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.appbitowear.constants.General

@SuppressLint("WearPasswordInput")
@Composable
fun WearTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: Int = EditorInfo.IME_ACTION_DONE,
    onImeAction: () -> Unit = {},
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnImeAction by rememberUpdatedState(onImeAction)
    val currentValue by rememberUpdatedState(value)
    val currentImeAction by rememberUpdatedState(imeAction)

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                EditText(context).apply {
                    setBackgroundResource(android.R.color.transparent)
                    setTextColor(android.graphics.Color.WHITE)
                    setHintTextColor(android.graphics.Color.GRAY)
                    textSize= 14f
                    hint = label
                    imeOptions = imeAction
                    isSingleLine = singleLine
                    if (!singleLine) this.maxLines = maxLines

                    var baseInputType = InputType.TYPE_CLASS_TEXT
                    if (keyboardType == KeyboardType.Email) {
                        baseInputType = baseInputType or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    } else if (isPassword) {
                        baseInputType = baseInputType or if (isPasswordVisible)
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        else
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    if (!singleLine) {
                        baseInputType = baseInputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }

                    this.inputType = baseInputType
                    this.typeface = android.graphics.Typeface.DEFAULT

                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            p0: CharSequence?, p1: Int, p2: Int, p3: Int ) {}

                        override fun onTextChanged(
                            p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            val newText = s?.toString() ?: General.EMPTY_STRING
                            if (newText != currentValue) {
                                currentOnValueChange(newText)
                            }
                        }
                    })

                    setOnEditorActionListener { view, actionId, _ ->
                        if (actionId == currentImeAction) {
                            currentOnImeAction()
                            view.clearFocus()
                            false
                        } else true
                    }
                }
            },

            update = { editText ->

                if (editText.text.toString() != value) {
                    editText.setText(value)
                    editText.setSelection(value.length)
                }

                if (isPassword) {
                    val targetVariation = if (isPasswordVisible) {
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    val currentVariation = editText.inputType and InputType.TYPE_MASK_VARIATION

                    if (currentVariation != targetVariation) {
                        editText.inputType = (
                                editText.inputType and InputType.TYPE_MASK_VARIATION.inv()
                        ) or targetVariation
                        editText.typeface = android.graphics.Typeface.DEFAULT
                        editText.setSelection(editText.text.length)
                    }
                }

            }
        )
        trailingIcon?.invoke()
    }
}
