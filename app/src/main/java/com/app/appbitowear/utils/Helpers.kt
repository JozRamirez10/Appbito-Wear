package com.app.appbitowear.utils

import com.app.appbitowear.constants.Auth
import com.app.appbitowear.data.local.TokenManager
import retrofit2.Response

object Helpers {

    fun clearBearerToken(token: String): String {
        return token.removePrefix(Auth.BEARER_PREFIX).trim()
    }

    fun extractTokenFromHeaders(response: Response<*>): String? {
        val authHeader = response.headers()[Auth.AUTHORIZATION_HEADER]
        return if (authHeader != null && authHeader.startsWith(Auth.BEARER_PREFIX)) {
            clearBearerToken(authHeader)
        } else {
            null
        }
    }
}

suspend fun TokenManager.extractAndSave(response: Response<*>): String? {
    val token = Helpers.extractTokenFromHeaders(response)
    if (token != null) {
        this.saveToken(token)
    }
    return token
}