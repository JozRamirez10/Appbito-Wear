package com.app.appbitowear.data.models.response

data class HabitProgress(
    val id: Int,
    val date: String,
    val timesPerformed: Int,
    val note: String? = null,
    val habitId: Int
)
