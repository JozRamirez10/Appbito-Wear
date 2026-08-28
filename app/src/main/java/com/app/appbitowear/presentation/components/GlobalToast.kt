package com.app.appbitowear.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.app.appbitowear.constants.Constants
import com.app.appbitowear.utils.ToastManager
import kotlinx.coroutines.delay

@Composable
fun GlobalToast(modifier: Modifier = Modifier) {
    val message by ToastManager.message.collectAsState()

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier.padding(bottom = 32.dp),
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        message?.let { msg ->
            Text(
                text = msg,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )

            LaunchedEffect(msg) {
                delay(Constants.TOAST_DELAY)
                ToastManager.clearToast()
            }
        }
    }

}