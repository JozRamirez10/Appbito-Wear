package com.app.appbitowear.repository

import com.app.appbitowear.data.models.response.User
import com.app.appbitowear.data.network.apis.UserApi
import com.app.appbitowear.utils.safeApiCall

class UserRepository(private val api: UserApi) {

    private var cachedUser: User? = null

    suspend fun getMe(forceRefresh: Boolean = false): Result<User> {
        if (!forceRefresh && cachedUser != null) {
            return Result.success(cachedUser!!)
        }

        return safeApiCall {
            val user = api.getMe()
            cachedUser = user
            user
        }
    }

    fun clearCache() {
        cachedUser = null
    }
}