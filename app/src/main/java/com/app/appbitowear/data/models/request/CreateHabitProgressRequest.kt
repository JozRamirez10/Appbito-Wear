package com.app.appbitowear.data.models.request

data class CreateHabitProgressRequest(
    val date: String,
    val timesPerformed: Int,
    val note: String? = null,
    val habitId: Int
)
