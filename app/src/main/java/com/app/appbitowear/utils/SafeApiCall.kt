package com.app.appbitowear.utils

import com.app.appbitowear.constants.ErrorInterceptors
import com.app.appbitowear.constants.General
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection

class ApiException(val code: Int, message: String): Exception(message)

private val GLOBAL_ERROR_CODES = setOf(
    HttpURLConnection.HTTP_INTERNAL_ERROR,
    HttpURLConnection.HTTP_BAD_GATEWAY,
    HttpURLConnection.HTTP_UNAVAILABLE,
    HttpURLConnection.HTTP_FORBIDDEN
)

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val finalException = when (e) {
            is IOException -> Exception(ErrorInterceptors.MSG_ERROR_ZERO)
            is HttpException -> ApiException(e.code(), getDefaultErrorMessage(e.code()))
            is ApiException -> e
            else -> Exception(e.message ?: ErrorInterceptors.MSG_ERROR_DEFAULT)
        }

        val shouldShowGlobalToast = when (finalException) {
            is ApiException -> finalException.code in GLOBAL_ERROR_CODES
            else -> true
        }

        if (shouldShowGlobalToast) {
            ToastManager.showToast(finalException.message ?: General.ERROR)
        }

        Result.failure(finalException)
    }
}

fun <T> Response<T>.checkSuccessOrThrow() {
    if (!isSuccessful) {
        throw ApiException(code(), getDefaultErrorMessage(code()))
    }
}

private fun getDefaultErrorMessage(code: Int): String {
    return when (code) {
        HttpURLConnection.HTTP_INTERNAL_ERROR,
        HttpURLConnection.HTTP_BAD_GATEWAY,
        HttpURLConnection.HTTP_UNAVAILABLE -> ErrorInterceptors.MSG_ERROR_500

        HttpURLConnection.HTTP_FORBIDDEN -> ErrorInterceptors.MSG_ERROR_FORBIDDEN
        HttpURLConnection.HTTP_BAD_REQUEST -> ErrorInterceptors.MSG_ERROR_BAD_REQUEST
        HttpURLConnection.HTTP_NOT_FOUND -> ErrorInterceptors.MSG_ERROR_NOT_FOUND

        else -> ErrorInterceptors.MSG_ERROR_DEFAULT
    }
}

fun handleApiError(error : Throwable, messagesMap: Map<Int, String>) {
    if (error is ApiException) {
        messagesMap[error.code]?.let { message ->
            ToastManager.showToast(message)
        }
    }
}