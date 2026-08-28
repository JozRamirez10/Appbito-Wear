package com.app.appbitowear.data.models.request

data class UpdateHabitProgressRequest(
    val timesPerformed: Int? = 0,
    val note: String? = null
)
