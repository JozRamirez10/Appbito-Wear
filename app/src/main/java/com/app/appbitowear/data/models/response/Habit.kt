package com.app.appbitowear.data.models.response

import com.app.appbitowear.enums.DaysOfWeek

data class Habit(
    val id: Int,
    val name: String,
    val description: String? = null,
    val hour: String? = null,
    val days: List<DaysOfWeek>
)