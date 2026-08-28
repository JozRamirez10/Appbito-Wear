package com.app.appbitowear.di

import android.content.Context
import com.app.appbitowear.data.local.TokenManager
import com.app.appbitowear.data.network.RetrofitClient
import com.app.appbitowear.data.network.apis.AuthApi
import com.app.appbitowear.data.network.apis.HabitApi
import com.app.appbitowear.data.network.apis.HabitProgressApi
import com.app.appbitowear.data.network.apis.UserApi
import com.app.appbitowear.data.network.interceptors.PersistentCookieJar
import com.app.appbitowear.repository.AuthRepository
import com.app.appbitowear.repository.HabitProgressRepository
import com.app.appbitowear.repository.HabitRepository
import com.app.appbitowear.repository.UserRepository
import com.app.appbitowear.utils.SessionManager

class AppContainer(private val context: Context) {

    val tokenManager by lazy { TokenManager(context) }

    val cookieJar by lazy { PersistentCookieJar(context) }

    private val retrofit by lazy {
        RetrofitClient.create(
            tokenManager = tokenManager,
            cookieJar = cookieJar,
            onSessionExpired = { SessionManager.triggerSessionExpired() }
        )
    }

    val authRepository by lazy {
        AuthRepository(RetrofitClient.getApi<AuthApi>(retrofit), tokenManager, cookieJar)
    }

    val habitRepository by lazy {
        HabitRepository(RetrofitClient.getApi<HabitApi>(retrofit))
    }

    val habitProgressRepository by lazy {
        HabitProgressRepository(RetrofitClient.getApi<HabitProgressApi>(retrofit))
    }

    val userRepository by lazy {
        UserRepository(RetrofitClient.getApi<UserApi>(retrofit))
    }
}