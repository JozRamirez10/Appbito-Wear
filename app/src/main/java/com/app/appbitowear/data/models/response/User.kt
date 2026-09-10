package com.app.appbitowear.data.models.response

data class User(
    val id: Int,
    val name: String,
    val lastname: String,
    val birthDate: String,
    val email: String,
    val image: String? = null,
    val enabled: Boolean
)
