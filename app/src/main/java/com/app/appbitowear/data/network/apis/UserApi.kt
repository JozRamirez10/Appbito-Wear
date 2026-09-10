package com.app.appbitowear.data.network.apis

import com.app.appbitowear.constants.ApiRoutes
import com.app.appbitowear.data.models.response.User
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface UserApi {

    @GET(ApiRoutes.USERS_ME)
    suspend fun getMe(): User

    @Streaming
    @GET(ApiRoutes.USERS_ME_IMAGE)
    suspend fun getMyImage(): ResponseBody
}