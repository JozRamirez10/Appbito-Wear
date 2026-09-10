package com.app.appbitowear.data.models.view

import com.app.appbitowear.data.models.response.Habit
import com.app.appbitowear.data.models.response.HabitProgress

data class HabitView(
    val habit: Habit,
    val streak: Int,
    val isDoneToday: Boolean = false,
    val todayProgress: HabitProgress? = null
)
