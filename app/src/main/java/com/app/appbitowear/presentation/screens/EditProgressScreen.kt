package com.app.appbitowear.presentation.screens

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.app.appbitowear.constants.Colors
import com.app.appbitowear.constants.Constants
import com.app.appbitowear.constants.General
import com.app.appbitowear.data.models.response.HabitProgress
import com.app.appbitowear.presentation.components.ActionChip
import com.app.appbitowear.presentation.components.FullScreenLoader
import com.app.appbitowear.presentation.components.ScalingLazyColumnCustom
import com.app.appbitowear.presentation.components.ScreenTitle
import com.app.appbitowear.presentation.components.WearTextField
import com.app.appbitowear.presentation.components.handleFocus
import com.app.appbitowear.presentation.viewmodels.EditProgressViewModel

@Composable
fun EditProgressScreen(
    viewModel: EditProgressViewModel,
    onSaveSuccess: (habitId: Int, finalProgress: HabitProgress?) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.STARTED
    )
    val focusManager = LocalFocusManager.current
    val listFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSaveSuccess(state.habitId, state.finalProgress)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            FullScreenLoader()
        } else {
            ScalingLazyColumnCustom(
                modifier = Modifier.fillMaxSize(),
                focusRequester =  listFocusRequester,
                contentPadding = PaddingValues(
                    top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ScreenTitle(text = state.habitName)
                }

                item {
                    ProgressStepper(
                        timesPerformed = state.timesPerformed,
                        onDecrement = viewModel::decrementTimes,
                        onIncrement = viewModel::incrementTimes
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WearTextField(
                            value = state.note,
                            onValueChange = viewModel::onNoteChanged,
                            label = General.QUICK_NOTE,
                            keyboardType = KeyboardType.Text,
                            singleLine = false,
                            maxLines = 3,
                            imeAction = EditorInfo.IME_ACTION_DONE,
                            onImeAction = { handleFocus(focusManager, listFocusRequester) }
                        )

                        val currentLength = state.note.length
                        Text(
                            text = "$currentLength / ${Constants.LIMIT_NOTE_LENGTH}",
                            color = if (currentLength >= Constants.LIMIT_NOTE_LENGTH) Color.Red
                                else Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(top = 4.dp, end = 16.dp)
                        )
                    }
                }

                item {
                    ActionChip(
                        text = General.SAVE,
                        backgroundColor = Colors.SUCCESS_GREEN,
                        onClick = {
                            handleFocus(
                                focusManager,
                                listFocusRequester,
                                viewModel::saveProgress
                            )
                        }
                    )
                }

                if (state.originalProgress != null) {
                    item {
                        ActionChip(
                            text = General.DELETE,
                            backgroundColor = Colors.WARNING_RED,
                            onClick = {
                                handleFocus(
                                    focusManager,
                                    listFocusRequester,
                                    viewModel::deleteProgress
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepper(
    timesPerformed: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Button(
            onClick = onDecrement,
            enabled = timesPerformed > 0,
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = General.DECREASE
            )
        }

        Text(
            text = "$timesPerformed",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
            onClick = onIncrement,
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = General.INCREASE
            )
        }
    }
}