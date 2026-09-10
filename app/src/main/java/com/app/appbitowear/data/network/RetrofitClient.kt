package com.app.appbitowear.data.network

import com.app.appbitowear.constants.Constants
import com.app.appbitowear.data.local.TokenManager
import com.app.appbitowear.data.network.apis.AuthApi
import com.app.appbitowear.data.network.interceptors.AuthInterceptor
import com.app.appbitowear.data.network.interceptors.PersistentCookieJar
import com.app.appbitowear.utils.extractAndSave
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun create(
        tokenManager: TokenManager,
        cookieJar: PersistentCookieJar,
        onSessionExpired: () -> Unit
    ): Retrofit {

        val basicClient = buildOkHttpClient(cookieJar)
        val basicRetrofit = buildRetrofit(basicClient)
        val authApi = basicRetrofit.create(AuthApi::class.java)

        val authInterceptor = AuthInterceptor(
            tokenManager = tokenManager,
            onSessionExpired = onSessionExpired,
            onRefreshToken = {
                runCatching {
                    val response = authApi.refreshToken()
                    if (response.isSuccessful) tokenManager.extractAndSave(response) else null
                }.getOrNull()
            }
        )

        val mainClient = buildOkHttpClient(cookieJar, authInterceptor)
        return buildRetrofit(mainClient, includeScalars = true)
    }

    private fun buildOkHttpClient(
        cookieJar: PersistentCookieJar,
        interceptor: Interceptor? = null
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            cookieJar(cookieJar)
            connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            interceptor?.let { addInterceptor(it) }
        }.build()
    }

    private fun buildRetrofit(
        client: OkHttpClient,
        includeScalars: Boolean = false
    ): Retrofit {
        return Retrofit.Builder().apply {
            baseUrl(Constants.BASE_URL)
            client(client)
            if (includeScalars) {
                addConverterFactory(ScalarsConverterFactory.create())
            }
            addConverterFactory(GsonConverterFactory.create())
        }.build()
    }

    inline fun <reified T> getApi(retrofit: Retrofit): T = retrofit.create(T::class.java)
}