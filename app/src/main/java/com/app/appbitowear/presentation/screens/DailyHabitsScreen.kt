package com.app.appbitowear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.Text
import com.app.appbitowear.constants.App
import com.app.appbitowear.constants.Constants
import com.app.appbitowear.constants.DailyHabits
import com.app.appbitowear.constants.General
import com.app.appbitowear.constants.UserProfile
import com.app.appbitowear.data.models.view.HabitView
import com.app.appbitowear.presentation.components.ActionChip
import com.app.appbitowear.presentation.components.FullScreenLoader
import com.app.appbitowear.presentation.components.ScalingLazyColumnCustom
import com.app.appbitowear.presentation.components.ScreenTitle
import com.app.appbitowear.presentation.viewmodels.DailyHabitsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyHabitsScreen(
    viewModel : DailyHabitsViewModel,
    onEditProgress: (Int, String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.STARTED
    )
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.loadTodayHabits()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT))
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
                contentPadding = PaddingValues(
                    top = 32.dp, bottom = 24.dp, start = 8.dp, end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item{
                    ScreenTitle("${App.DAILY_HABITS} - $todayFormatted")
                }

                if (!state.isNetworkAvailable) {
                    item {
                        Text(
                            text = DailyHabits.OFFLINE_MODE,
                            color = Color.Red,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (state.habits.isEmpty()) {
                    item {
                        Text(
                            text = DailyHabits.THERE_ARENT_HABITS_TODAY,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(
                        items = state.habits,
                        key = { habitView -> habitView.habit.id },
                    ) { habitView ->
                        HabitItemRow(
                            habitView = habitView,
                            isInteractive = state.isInteractive,
                            onToggleDone = viewModel::toggleHabitDone,
                            onEditProgress = onEditProgress
                        )
                    }
                }

                item {
                    ActionChip(
                        text = UserProfile.PROFILE,
                        backgroundColor = Color.DarkGray,
                        onClick = onNavigateToProfile,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitItemRow(
    habitView: HabitView,
    isInteractive: Boolean,
    onToggleDone: (HabitView) -> Unit,
    onEditProgress: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = habitView.isDoneToday
    val habitName = habitView.habit.name
    val streak = habitView.streak
    val hasNote = !habitView.todayProgress?.note.isNullOrEmpty()
    val habitId = habitView.habit.id

    val handleToggle = remember(habitView, onToggleDone) {
        { onToggleDone(habitView) }
    }
    val handleEdit = remember(habitView, habitName, onEditProgress) {
        { onEditProgress(habitId, habitName)}
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = handleToggle,
                enabled = isInteractive,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Filled.CheckCircle
                        else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = General.DONE,
                    tint = if (isDone) Color.White else Color.LightGray
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = habitName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${General.STREAK}${General.COLON} $streak",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            if (hasNote) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = General.HAS_NOTE,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
            }

            IconButton(
                onClick = handleEdit,
                enabled = isInteractive,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = General.EDIT_PROGRESS,
                    tint = Color.White
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.DarkGray)
        )
    }
}